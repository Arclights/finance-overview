package com.arclights.finance_overview.services

import com.arclights.finance_overview.persistence.entities.CalculatedTransactionDefinition
import com.arclights.finance_overview.persistence.entities.RecurringTransaction
import com.arclights.finance_overview.persistence.entities.Statement
import com.arclights.finance_overview.persistence.entities.Taxonomy
import com.arclights.finance_overview.persistence.entities.TaxonomyType
import com.arclights.finance_overview.persistence.entities.Transaction
import com.arclights.finance_overview.persistence.entities.Transaction.TransactionType.Expense
import com.arclights.finance_overview.persistence.entities.Transaction.TransactionType.Income
import com.arclights.finance_overview.persistence.entities.Variable
import com.arclights.finance_overview.persistence.entities.Variable.VariableType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VariableEvaluatorTest {
    private val evaluator = VariableEvaluator()

    private val taxonomyType = TaxonomyType(id = UUID.randomUUID(), name = "Category")
    private val electricity = Taxonomy(id = UUID.randomUUID(), name = "Electricity", taxonomyType = taxonomyType)
    private val groceries = Taxonomy(id = UUID.randomUUID(), name = "Groceries", taxonomyType = taxonomyType)
    private val source = RecurringTransaction(
        id = UUID.randomUUID(),
        name = "Electricity",
        type = Expense,
        amount = BigDecimal("800"),
        taxonomies = setOf(electricity)
    )

    @Test
    fun `static evaluates to its static value regardless of statement`() {
        val variable = Variable(name = "Const", type = VariableType.Static, staticValue = BigDecimal("123.45"))

        assertThat(evaluator.evaluate(variable, statement(Month.JANUARY, 2026), listOf()))
            .isEqualByComparingTo("123.45")
    }

    @Test
    fun `current period sum adds matching transactions in the source's direction`() {
        val statement = statement(
            Month.JANUARY, 2026,
            tx(Expense, "1100", electricity),
            tx(Expense, "500", groceries)
        )

        assertThat(evaluator.evaluate(currentPeriodSum(), statement, listOf(statement)))
            .isEqualByComparingTo("1100")
    }

    @Test
    fun `current period sum nets opposite-typed matches against the source`() {
        val statement = statement(
            Month.JANUARY, 2026,
            tx(Expense, "1100", electricity),
            tx(Income, "200", electricity)
        )

        assertThat(evaluator.evaluate(currentPeriodSum(), statement, listOf(statement)))
            .isEqualByComparingTo("900")
    }

    @Test
    fun `current period sum matches on any of the source's taxonomies`() {
        val multiTaxonomySource = source.copy(taxonomies = setOf(electricity, groceries))
        val statement = statement(
            Month.JANUARY, 2026,
            tx(Expense, "1100", electricity),
            tx(Expense, "500", groceries)
        )
        val variable = currentPeriodSum().copy(recurringTransaction = multiTaxonomySource)

        assertThat(evaluator.evaluate(variable, statement, listOf(statement)))
            .isEqualByComparingTo("1600")
    }

    @Test
    fun `calculated transactions are never matched`() {
        val definition = CalculatedTransactionDefinition(
            id = UUID.randomUUID(),
            name = "Buffer",
            variableA = currentPeriodSum(),
            variableB = currentPeriodSum()
        )
        val statement = statement(
            Month.JANUARY, 2026,
            tx(Expense, "1100", electricity),
            tx(Expense, "250", electricity, calculatedTransactionDefinition = definition)
        )

        assertThat(evaluator.evaluate(currentPeriodSum(), statement, listOf(statement)))
            .isEqualByComparingTo("1100")
    }

    @Test
    fun `current period sum is zero when nothing matches`() {
        val statement = statement(Month.JANUARY, 2026, tx(Expense, "500", groceries))

        assertThat(evaluator.evaluate(currentPeriodSum(), statement, listOf(statement)))
            .isEqualByComparingTo("0")
    }

    @Test
    fun `average uses only the window of statements preceding the evaluated one`() {
        val statements = listOf(
            statement(Month.SEPTEMBER, 2025, tx(Expense, "5000", electricity)), // outside the window
            statement(Month.OCTOBER, 2025, tx(Expense, "700", electricity)),
            statement(Month.NOVEMBER, 2025, tx(Expense, "800", electricity)),
            statement(Month.DECEMBER, 2025, tx(Expense, "900", electricity)),
        )
        val evaluated = statement(Month.JANUARY, 2026, tx(Expense, "9999", electricity))
        val later = statement(Month.FEBRUARY, 2026, tx(Expense, "9999", electricity))

        assertThat(evaluator.evaluate(average(windowSize = 3), evaluated, ordered(statements + evaluated + later)))
            .isEqualByComparingTo("800")
    }

    @Test
    fun `average falls back when fewer preceding statements exist than the window`() {
        val statements = listOf(
            statement(Month.NOVEMBER, 2025, tx(Expense, "800", electricity)),
            statement(Month.DECEMBER, 2025, tx(Expense, "900", electricity)),
        )
        val evaluated = statement(Month.JANUARY, 2026)

        assertThat(evaluator.evaluate(average(windowSize = 3), evaluated, ordered(statements + evaluated)))
            .isEqualByComparingTo("750")
    }

    @Test
    fun `average falls back when a statement in the window has no matching transactions`() {
        val statements = listOf(
            statement(Month.SEPTEMBER, 2025, tx(Expense, "700", electricity)),
            statement(Month.OCTOBER, 2025, tx(Expense, "700", electricity)),
            statement(Month.NOVEMBER, 2025, tx(Expense, "500", groceries)),
            statement(Month.DECEMBER, 2025, tx(Expense, "900", electricity)),
        )
        val evaluated = statement(Month.JANUARY, 2026)

        assertThat(evaluator.evaluate(average(windowSize = 3), evaluated, ordered(statements + evaluated)))
            .isEqualByComparingTo("750")
    }

    @Test
    fun `average sums per statement and nets refunds before averaging`() {
        val statements = listOf(
            statement(Month.NOVEMBER, 2025, tx(Expense, "600", electricity), tx(Expense, "400", electricity)),
            statement(Month.DECEMBER, 2025, tx(Expense, "1100", electricity), tx(Income, "100", electricity)),
        )
        val evaluated = statement(Month.JANUARY, 2026)

        assertThat(evaluator.evaluate(average(windowSize = 2), evaluated, ordered(statements + evaluated)))
            .isEqualByComparingTo("1000")
    }

    @Test
    fun `average rounds to two decimals`() {
        val statements = listOf(
            statement(Month.OCTOBER, 2025, tx(Expense, "100", electricity)),
            statement(Month.NOVEMBER, 2025, tx(Expense, "100", electricity)),
            statement(Month.DECEMBER, 2025, tx(Expense, "101", electricity)),
        )
        val evaluated = statement(Month.JANUARY, 2026)

        assertThat(evaluator.evaluate(average(windowSize = 3), evaluated, ordered(statements + evaluated)))
            .isEqualTo(BigDecimal("100.33"))
    }

    @Test
    fun `chronological order sorts by year before month`() {
        val december = statement(Month.DECEMBER, 2025)
        val january = statement(Month.JANUARY, 2026)
        val march = statement(Month.MARCH, 2025)

        assertThat(listOf(january, december, march).sortedWith(Statement.chronologicalOrder))
            .containsExactly(march, december, january)
    }

    private fun currentPeriodSum() =
        Variable(id = UUID.randomUUID(), name = "Actual", type = VariableType.CurrentPeriodSum, recurringTransaction = source)

    private fun average(windowSize: Int) = Variable(
        id = UUID.randomUUID(),
        name = "Avg",
        type = VariableType.Average,
        recurringTransaction = source,
        windowSize = windowSize,
        fallbackAverage = BigDecimal("750")
    )

    private fun ordered(statements: List<Statement>) = statements.shuffled().sortedWith(Statement.chronologicalOrder)

    private class TxSpec(
        val type: Transaction.TransactionType,
        val amount: String,
        val taxonomy: Taxonomy,
        val calculatedTransactionDefinition: CalculatedTransactionDefinition?
    )

    private fun tx(
        type: Transaction.TransactionType,
        amount: String,
        taxonomy: Taxonomy,
        calculatedTransactionDefinition: CalculatedTransactionDefinition? = null
    ) = TxSpec(type, amount, taxonomy, calculatedTransactionDefinition)

    private fun statement(month: Month, year: Int, vararg transactions: TxSpec): Statement {
        val statement = Statement(id = UUID.randomUUID(), month = month, year = year)
        return statement.copy(transactions = transactions.map {
            Transaction(
                id = UUID.randomUUID(),
                originalName = "tx",
                date = LocalDate.of(year, month, 1),
                statement = statement,
                type = it.type,
                amount = BigDecimal(it.amount),
                taxonomies = setOf(it.taxonomy),
                calculatedTransactionDefinition = it.calculatedTransactionDefinition
            )
        })
    }
}
