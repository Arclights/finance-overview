package com.arclights.finance_overview.services

import com.arclights.finance_overview.persistence.entities.RecurringTransaction
import com.arclights.finance_overview.persistence.entities.Statement
import com.arclights.finance_overview.persistence.entities.Transaction
import com.arclights.finance_overview.persistence.entities.Variable
import com.arclights.finance_overview.persistence.entities.Variable.VariableType
import com.arclights.finance_overview.persistence.repositories.StatementRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.math.RoundingMode

@Singleton
class VariableEvaluator {

    @Inject
    private lateinit var statementRepository: StatementRepository

    fun evaluate(variable: Variable, statement: Statement): BigDecimal =
        evaluate(variable, statement, statementRepository.findAll().sortedWith(Statement.chronologicalOrder))

    // Pure evaluation: orderedStatements must be every statement, sorted by Statement.chronologicalOrder.
    fun evaluate(variable: Variable, statement: Statement, orderedStatements: List<Statement>): BigDecimal =
        when (variable.type) {
            VariableType.Static -> checkNotNull(variable.staticValue) { "Static variable ${variable.id} has no staticValue" }
            VariableType.CurrentPeriodSum -> sumOfMatching(statement, source(variable))
            VariableType.Average -> average(variable, statement, orderedStatements)
        }

    // Averages over the windowSize statements immediately preceding this one. Falls back to
    // fallbackAverage unless every statement in that window has at least one matching transaction.
    private fun average(variable: Variable, statement: Statement, orderedStatements: List<Statement>): BigDecimal {
        val windowSize = checkNotNull(variable.windowSize) { "Average variable ${variable.id} has no windowSize" }
        val fallbackAverage = checkNotNull(variable.fallbackAverage) { "Average variable ${variable.id} has no fallbackAverage" }
        val source = source(variable)

        val window = orderedStatements
            .filter { Statement.chronologicalOrder.compare(it, statement) < 0 }
            .takeLast(windowSize)
            .filter { preceding -> preceding.transactions.any { isMatching(it, source) } }

        if (window.size < windowSize) {
            return fallbackAverage
        }

        return window
            .sumOf { sumOfMatching(it, source) }
            .divide(windowSize.toBigDecimal(), 2, RoundingMode.HALF_UP)
    }

    // Transactions of the source's own type add to the sum and opposite-typed ones subtract
    // (e.g. a refund nets against the bill), so the result is in the source's direction.
    private fun sumOfMatching(statement: Statement, source: RecurringTransaction): BigDecimal =
        statement.transactions
            .filter { isMatching(it, source) }
            .sumOf { if (it.type == source.type) it.amount else it.amount.negate() }

    private fun isMatching(transaction: Transaction, source: RecurringTransaction): Boolean {
        val sourceTaxonomyIds = source.taxonomies.map { it.id }.toSet()
        return transaction.calculatedTransactionDefinition == null &&
            transaction.taxonomies.any { it.id in sourceTaxonomyIds }
    }

    private fun source(variable: Variable): RecurringTransaction =
        checkNotNull(variable.recurringTransaction) { "${variable.type} variable ${variable.id} has no recurringTransaction" }
}
