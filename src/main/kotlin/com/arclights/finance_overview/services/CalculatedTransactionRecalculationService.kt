package com.arclights.finance_overview.services

import com.arclights.finance_overview.persistence.entities.CalculatedTransactionDefinition
import com.arclights.finance_overview.persistence.entities.Statement
import com.arclights.finance_overview.persistence.entities.Transaction
import com.arclights.finance_overview.persistence.entities.Variable
import com.arclights.finance_overview.persistence.entities.Variable.VariableType
import com.arclights.finance_overview.persistence.repositories.CalculatedTransactionDefinitionRepository
import com.arclights.finance_overview.persistence.repositories.StatementRepository
import com.arclights.finance_overview.persistence.repositories.TransactionRepository
import com.arclights.finance_overview.persistence.repositories.VariableRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

// Keeps the generated transactions of calculated transaction definitions in sync (spec §5.2).
// Every trigger runs inside the caller's transaction, so a cascade commits or rolls back with
// the write that caused it.
@Singleton
open class CalculatedTransactionRecalculationService {

    @Inject
    private lateinit var variableEvaluator: VariableEvaluator

    @Inject
    private lateinit var variableRepository: VariableRepository

    @Inject
    private lateinit var calculatedTransactionDefinitionRepository: CalculatedTransactionDefinitionRepository

    @Inject
    private lateinit var statementRepository: StatementRepository

    @Inject
    private lateinit var transactionRepository: TransactionRepository

    @Inject
    private lateinit var entityManager: EntityManager

    // Trigger 1: transactions tagged with any of taxonomyIds were created, updated or deleted in
    // statementId. Pass the union of taxonomies before and after the change, so variables a
    // transaction stopped matching are recalculated too. May create missing rows, but only in
    // the statements the change can affect.
    @Transactional
    open fun onTransactionsChanged(statementId: UUID, taxonomyIds: Set<UUID>) {
        val variables = variableRepository.findAll().filter { variable ->
            variable.recurringTransaction?.taxonomies?.any { it.id in taxonomyIds } == true
        }
        if (variables.isEmpty()) return

        val context = loadContext()
        val changedIndex = context.orderedStatements.indexOfFirst { it.id == statementId }
        val affectedStatementIdsByVariableId = variables.associate { variable ->
            variable.id!! to affectedStatements(variable, changedIndex, context.orderedStatements).map { it.id!! }
        }

        val targets = calculatedTransactionDefinitionRepository.findAll().flatMap { definition ->
            listOf(definition.variableA.id, definition.variableB.id)
                .flatMap { affectedStatementIdsByVariableId[it].orEmpty() }
                .map { statementId -> definition to statementId }
        }
        recalculate(targets, context, createMissing = true)
    }

    // Trigger 2: backfills a generated row in every existing statement.
    @Transactional
    open fun onDefinitionCreated(definition: CalculatedTransactionDefinition) {
        val context = loadContext()
        recalculate(context.orderedStatements.map { definition to it.id!! }, context, createMissing = true)
    }

    // Trigger 3: recalculates the definition's existing rows.
    @Transactional
    open fun onDefinitionChanged(definition: CalculatedTransactionDefinition) =
        recalculateExisting(listOf(definition))

    // Trigger 4: recalculates the existing rows of every definition using the variable.
    @Transactional
    open fun onVariableChanged(variable: Variable) =
        recalculateExisting(
            calculatedTransactionDefinitionRepository.findAll().filter { definition ->
                definition.variableA.id == variable.id || definition.variableB.id == variable.id
            }
        )

    private fun recalculateExisting(definitions: List<CalculatedTransactionDefinition>) {
        if (definitions.isEmpty()) return

        val context = loadContext()
        val targets = definitions.flatMap { definition ->
            context.orderedStatements
                .filter { context.generatedRow(definition, it.id!!) != null }
                .map { definition to it.id!! }
        }
        recalculate(targets, context, createMissing = false)
    }

    // A CurrentPeriodSum variable only reads its own statement. An Average variable is read by
    // the windowSize statements after the changed one, whose windows include it.
    private fun affectedStatements(variable: Variable, changedIndex: Int, orderedStatements: List<Statement>): List<Statement> =
        when {
            changedIndex < 0 -> listOf()
            variable.type == VariableType.CurrentPeriodSum -> listOf(orderedStatements[changedIndex])
            variable.type == VariableType.Average ->
                orderedStatements.drop(changedIndex + 1).take(variable.windowSize!!)
            else -> listOf()
        }

    private fun recalculate(
        targets: List<Pair<CalculatedTransactionDefinition, UUID>>,
        context: Context,
        createMissing: Boolean
    ) {
        // Distinct so a definition whose two variables both point at a statement is only
        // written once; a second insert would violate uq_calculated_transaction_per_statement.
        targets.distinctBy { (definition, statementId) -> definition.id to statementId }
            .forEach { (definition, statementId) ->
                val existing = context.generatedRow(definition, statementId)
                if (existing == null && !createMissing) return@forEach

                val statement = context.evaluatedStatement(statementId)
                val delta = variableEvaluator.evaluate(definition.variableA, statement, context.orderedStatements)
                    .subtract(variableEvaluator.evaluate(definition.variableB, statement, context.orderedStatements))

                val row = (existing ?: newRow(definition, context.managedStatement(statementId))).copy(
                    originalName = definition.name,
                    date = LocalDate.of(statement.year, statement.month, 1),
                    type = if (delta < BigDecimal.ZERO) Transaction.TransactionType.Income else Transaction.TransactionType.Expense,
                    amount = delta.abs(),
                    taxonomies = definition.resultTaxonomies.toHashSet()
                )
                if (existing == null) transactionRepository.save(row) else transactionRepository.update(row)
            }
    }

    private fun newRow(definition: CalculatedTransactionDefinition, statement: Statement) = Transaction(
        originalName = definition.name,
        date = LocalDate.of(statement.year, statement.month, 1),
        statement = statement,
        type = Transaction.TransactionType.Expense,
        amount = BigDecimal.ZERO,
        taxonomies = setOf(),
        calculatedTransactionDefinition = definition
    )

    // Flushes pending writes, then reloads every transaction. A managed Statement's
    // transactions collection doesn't see rows written earlier in the same session, so
    // evaluation runs on copies carrying the fresh lists instead.
    private fun loadContext(): Context {
        entityManager.flush()
        val transactionsByStatementId = transactionRepository.findAll().groupBy { it.statement.id!! }
        val managedStatements = statementRepository.findAll().sortedWith(Statement.chronologicalOrder)
        val orderedStatements = managedStatements.map { it.copy(transactions = transactionsByStatementId[it.id].orEmpty()) }
        return Context(managedStatements.associateBy { it.id!! }, orderedStatements)
    }

    private class Context(
        private val managedStatementsById: Map<UUID, Statement>,
        val orderedStatements: List<Statement>
    ) {
        private val evaluatedStatementsById = orderedStatements.associateBy { it.id!! }

        fun managedStatement(statementId: UUID) = managedStatementsById.getValue(statementId)

        fun evaluatedStatement(statementId: UUID) = evaluatedStatementsById.getValue(statementId)

        fun generatedRow(definition: CalculatedTransactionDefinition, statementId: UUID): Transaction? =
            evaluatedStatement(statementId).transactions
                .find { it.calculatedTransactionDefinition?.id == definition.id }
    }
}
