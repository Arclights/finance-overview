# Feature: Variables & Calculated Transactions

This is split into two composable features:

- **Variables** (§3): a named, reusable value — either a constant or something
  derived from matching transactions. Self-contained; usable anywhere a value
  is needed.
- **Calculated Transactions** (§4): a transaction whose amount is
  `variable A − variable B`, materialized as a real transaction in a
  statement.

## 1. Motivation

Some expenses are billed at a flat/estimated rate but the *actual* cost
varies month to month (e.g. electricity, trued up against real usage). We
want a transaction that automatically banks the difference between a
historical average and the current month's actual into/out of a buffer —
and we want the building blocks (the average, the actual) to be reusable
values in their own right, not something baked one-off into a single
feature.

**Worked example**, expressed in the new terms:

- Variable **"Electricity avg (12mo)"**: type Average, source =
  `RecurringTransaction` "Electricity", window = 12, fallback = 800 kr.
  Evaluates to 850 kr for the statement in question.
- Variable **"Electricity actual"**: type CurrentPeriodSum, source =
  `RecurringTransaction` "Electricity". Evaluates to 1 100 kr for the same
  statement.
- Calculated Transaction Definition **"Electricity buffer"**: variableA =
  "Electricity avg (12mo)", variableB = "Electricity actual".
  `850 − 1100 = −250` → **Income** of 250 kr (withdraw from buffer to cover
  the overage this month).

## 2. Terminology

| Term | Meaning |
|---|---|
| **Variable** | A named, reusable value. Either constant (Static) or derived from transactions matching a source (Average, CurrentPeriodSum). Self-contained — never references another Variable. |
| **Source** | The `RecurringTransaction` a Variable is linked to (Average/CurrentPeriodSum only). Its taxonomies determine which real transactions "match" for that Variable. |
| **Calculated Transaction Definition** | A saved configuration referencing two Variables; produces a materialized `Transaction` per statement. |
| **Calculated Transaction** | The materialized `Transaction` row generated from a Calculated Transaction Definition for a specific statement. |
| **Window** | (Average variables only) number of preceding statements averaged over. |
| **Fallback average** | (Average variables only) constant used when fewer statements of history exist than the window requires. |

## 3. Feature: Variables

### 3.1 Types

| Type | Value | Configuration |
|---|---|---|
| **Static** | A fixed constant. | `staticValue: decimal` |
| **Average** | Average of matching transactions, summed per statement then averaged, over the preceding `windowSize` statements. If fewer than `windowSize` prior statements have matching data, use `fallbackAverage` instead. | `recurringTransactionId`, `windowSize`, `fallbackAverage` |
| **CurrentPeriodSum** | Sum of matching transactions in *the statement being evaluated*. | `recurringTransactionId` |

"Matching transactions" (Average, CurrentPeriodSum) = transactions in the
relevant statement(s) tagged with (at least one of) the linked
`RecurringTransaction`'s taxonomies, excluding any transaction that has
`calculatedTransactionDefinitionId` set (a calculated transaction is never
matched as a source — prevents a variable from indirectly depending on its
own output).

### 3.2 Evaluation model

A Variable has **no stored value of its own** — it is evaluated fresh, in
the context of a specific statement, whenever something (a Calculated
Transaction Definition) reads it. Static variables ignore the statement
context; Average and CurrentPeriodSum don't.

Variables are **self-contained**: a Variable's definition never references
another Variable. No cycle detection is needed within this feature —
composability happens one level up, in Calculated Transaction Definitions.

### 3.3 Data model

New table: `variables`

| Name | Type | Comment |
|---|---|---|
| id | uuid | |
| name | text | e.g. "Electricity avg (12mo)" |
| type | enum | `Static` \| `Average` \| `CurrentPeriodSum` |
| staticValue | decimal, nullable | Set iff `type = Static` |
| recurringTransactionId | fk, nullable | Set iff `type = Average \| CurrentPeriodSum`; defines the source taxonomies for matching |
| windowSize | int, nullable | Set iff `type = Average` |
| fallbackAverage | decimal, nullable | Set iff `type = Average` |
| created_at | timestamp | |
| updated_at | timestamp | |

### 3.4 API

```
GET    /v1/variables
POST   /v1/variables
GET    /v1/variables/<id>
PUT    /v1/variables/<id>   # cascades recalculation, see §5.4
DELETE /v1/variables/<id>   # blocked (409) while referenced by a Calculated Transaction Definition
```

## 4. Feature: Calculated Transactions

### 4.1 Formula

Fixed shape — not a free expression:

```
delta = evaluate(variableA, statement) - evaluate(variableB, statement)

if delta > 0:  generate Expense, amount = delta
if delta < 0:  generate Income,  amount = abs(delta)
if delta == 0: generate Expense, amount = 0   (row kept, not deleted — see §5.3)
```

A Calculated Transaction Definition's `variableA`/`variableB` must be
Variables (§3), never another Calculated Transaction Definition. This
mirrors the no-chaining rule already inherent in Variables being
self-contained, keeping the whole dependency graph exactly two levels deep:
`CalculatedTransactionDefinition → Variable → (real transactions | constant)`.

### 4.2 Data model

New table: `calculated_transaction_definitions`

| Name | Type | Comment |
|---|---|---|
| id | uuid | |
| name | text | e.g. "Electricity buffer" |
| variableAId | fk → variables | |
| variableBId | fk → variables | |
| resultTaxonomyIds | fk[] (join table) | Taxonomies applied to the generated transaction — explicit, not inherited from either variable's source (so the generated buffer transaction doesn't get miscounted as "actual" spend by other definitions/reports) |
| created_at | timestamp | |
| updated_at | timestamp | |

`transactions` table: add one nullable column, mirroring the existing
`recurringTransaction` link:

| Name | Type | Comment |
|---|---|---|
| calculatedTransactionDefinitionId | fk, nullable | Set when this row was generated by a Calculated Transaction Definition |

A transaction with this FK set is system-managed: its `amount` and `type`
are recalculated automatically (§5) and should not be directly hand-edited
via the normal transaction edit path (or if it is, that edit is understood
to be overwritten on the next recalculation).

### 4.3 API

```
GET    /v1/calculated-transaction-definitions
POST   /v1/calculated-transaction-definitions
GET    /v1/calculated-transaction-definitions/<id>
PUT    /v1/calculated-transaction-definitions/<id>   # cascades recalculation, see §5.4
DELETE /v1/calculated-transaction-definitions/<id>   # blocked (409) while generated rows exist, see §5.3
```

No changes needed to statement/transaction read endpoints — calculated
transactions are ordinary rows in `transactions` once materialized, so they
already appear in `/v1/statements/<id>/transactions` and
`/v1/statements/<id>/transactions/sum`. Consider adding
`calculatedTransactionDefinitionId` to `TransactionDto` so the frontend can
visually mark system-generated rows and prevent free editing of their
amount.

## 5. Lifecycle & recalculation

Calculated transactions are **materialized** (a real `Transaction` row) and
kept in sync automatically — there is no manual recalculate action anywhere
in either feature. Variables themselves are never materialized (§3.2) — only
the final Calculated Transaction result is stored.

### 5.1 Recurring placeholder suppression

A `RecurringTransaction` referenced as the source (`recurringTransactionId`)
of any Average or CurrentPeriodSum Variable **stops generating its own flat
monthly placeholder** when a statement is created. This applies as soon as a
Variable references it — independent of whether a Calculated Transaction
Definition currently uses that Variable — since the point is to avoid the
placeholder polluting the "matching transactions" set (§3.1) that the
Variable sums/averages over.

Implication for `StatementService.createStatement`: when seeding recurring
placeholders for a new statement, skip any `RecurringTransaction` that is
referenced by any `variables` row.

### 5.2 Recalculation triggers

A Calculated Transaction Definition's generated row for a given statement is
recalculated whenever:

1. **A source transaction changes.** A transaction matching the
   `recurringTransactionId` of a Variable used (as variableA or variableB)
   by the definition is created, updated, or deleted.
   - If the Variable is `CurrentPeriodSum`, this only affects the statement
     the change occurred in.
   - If the Variable is `Average`, this affects every statement whose
     trailing window includes the changed statement — i.e. up to
     `windowSize` statements *after* the one that changed, not just the one
     that changed. (Correcting a bill from 8 months ago shifts every
     average that included it.)
2. **The Calculated Transaction Definition is created** — backfills a
   calculated transaction for every existing statement it applies to.
3. **The Calculated Transaction Definition is edited** (variableA/variableB
   swapped) — recalculates every statement it applies to.
4. **A Variable it uses is edited** (type, static value, source, window, or
   fallback changed) — recalculates every statement of every Calculated
   Transaction Definition that references that Variable, since a shared
   Variable can be used by more than one definition.

Creating a new statement does **not** eagerly seed a calculated transaction
the way recurring placeholders are seeded — a calculated transaction for a
statement only comes into existence via triggers 1–4 above.

### 5.3 Zero-delta and deletion behavior

A `delta == 0` recalculation still produces a visible transaction row with
`amount = 0` — not deleted, so the statement retains a full audit trail of
the calculation having run.

- Deleting a **Calculated Transaction Definition** is blocked (`409`) while
  any generated transactions still reference it. They must be deleted
  first.
- Deleting a **Variable** is blocked (`409`) while any Calculated
  Transaction Definition still references it as variableA or variableB.

### 5.4 Performance note

Trigger 1's forward cascade (§5.2) combined with trigger 4's fan-out across
every definition sharing a Variable means a single transaction edit or
Variable edit can, in the worst case, touch `windowSize × (number of
definitions sharing that variable)` statements. For a personal-finance
dataset (tens to low hundreds of statements, a handful of definitions) this
is not a real performance concern, but each cascade should run inside one
transaction so a partial failure can't leave some statements recalculated
and others stale.

## 6. Out of scope for v1

- Free-text formula expressions / arbitrary functions — Calculated
  Transactions are always exactly `variableA − variableB` (§4.1); Variables
  are always exactly one of the three fixed types (§3.1).
- Variables referencing other Variables, or Calculated Transaction
  Definitions referencing other Calculated Transaction Definitions
  (chaining) — the dependency graph is fixed at two levels (§4.1).
- Any concept of a real "buffer account" balance/ledger — the buffer is
  implicit; this feature only produces the in/out transactions, it does not
  track a running buffer balance anywhere.
- A UI/API for previewing a Variable's evaluated value ad hoc (outside the
  context of a Calculated Transaction Definition) — could be a natural
  follow-up (`GET /v1/variables/<id>/value?statementId=...`) but isn't
  required for the electricity-buffer use case.
