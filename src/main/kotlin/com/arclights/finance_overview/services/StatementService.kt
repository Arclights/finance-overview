package com.arclights.finance_overview.services

import com.arclights.finance_overview.http.models.Balance
import com.arclights.finance_overview.http.models.StatementDto
import com.arclights.finance_overview.http.models.reponses.PageableResponse
import com.arclights.finance_overview.http.models.reponses.PersonSummaryV1
import com.arclights.finance_overview.http.models.reponses.PersonalSummedTransactionsV1
import com.arclights.finance_overview.http.models.reponses.SummedTransactionV1
import com.arclights.finance_overview.http.models.reponses.TopExpensesV1Response
import com.arclights.finance_overview.http.models.requests.CreateStatementRequest
import com.arclights.finance_overview.http.models.requests.PageableRequest
import com.arclights.finance_overview.mappers.TaxonomyMapper
import com.arclights.finance_overview.persistence.entities.Statement
import com.arclights.finance_overview.persistence.entities.Transaction
import com.arclights.finance_overview.persistence.repositories.TaxonomyRepository
import com.arclights.finance_overview.mappers.TransactionMapper
import com.arclights.finance_overview.persistence.repositories.RecurringTransactionRepository
import com.arclights.finance_overview.persistence.repositories.StatementRepository
import com.arclights.finance_overview.persistence.repositories.TransactionRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Singleton
open class StatementService {
    @Inject
    private lateinit var statementRepository: StatementRepository

    @Inject
    private lateinit var taxonomyRepository: TaxonomyRepository

    @Inject
    private lateinit var taxonomyMapper: TaxonomyMapper

    @Inject
    private lateinit var transactionMapper: TransactionMapper

    @Inject
    private lateinit var recurringTransactionRepository: RecurringTransactionRepository

    @Inject
    private lateinit var transactionRepository: TransactionRepository

    @Transactional
    open fun createStatement(createStatementRequest: CreateStatementRequest): StatementDto {
        val persons = if (createStatementRequest.personIds.isNotEmpty()) {
            val taxonomies = taxonomyRepository.findAllByIdIn(createStatementRequest.personIds.toSet())

            // Validate that all taxonomies are of type "Person"
            val invalidTaxonomies = taxonomies.filter { it.taxonomyType.name != "Person" }
            if (invalidTaxonomies.isNotEmpty()) {
                throw IllegalArgumentException(
                    "The following taxonomy IDs are not of type 'Person': ${invalidTaxonomies.map { it.id }}"
                )
            }

            // Validate that all requested IDs were found
            if (taxonomies.size != createStatementRequest.personIds.size) {
                val foundIds = taxonomies.map { it.id }.toSet()
                val missingIds = createStatementRequest.personIds.filterNot { it in foundIds }
                throw IllegalArgumentException("The following taxonomy IDs were not found: $missingIds")
            }

            taxonomies.toList()
        } else {
            listOf()
        }

        val statement = Statement(
            month = createStatementRequest.month,
            year = createStatementRequest.year,
            persons = persons
        )

        val savedStatement = statementRepository.save(statement)

        val recurringTransactions = recurringTransactionRepository.findAll().toList()
        if (recurringTransactions.isNotEmpty()) {
            val placeholderDate = LocalDate.of(savedStatement.year, savedStatement.month, 1)
            val placeholderTransactions = recurringTransactions.map { recurringTransaction ->
                transactionMapper.map(recurringTransaction, savedStatement, placeholderDate)
            }
            transactionRepository.saveAll(placeholderTransactions)
        }

        return mapStatement(savedStatement)
    }

    @Transactional
    open fun getStatement(id: UUID) = statementRepository.getById(id)
        ?.let(this::mapStatement)

    @Transactional
    open fun getStatements(pageableRequest: PageableRequest): PageableResponse<StatementDto> =
        // TODO: Convert to DTO
        statementRepository.findAll(pageableRequest.toPageable())
            .let {
                PageableResponse(
                    it.content.map(this::mapStatement),
                    it.pageNumber,
                    it.totalPages,
                    it.content.size
                )
            }

    private fun mapStatement(statement: Statement) = StatementDto(
        statement.id!!,
        statement.month,
        statement.year,
        Balance(
            statement.transactions.filter { it.type == Transaction.TransactionType.Income }.sumOf { it.amount }
                .subtract(statement.transactions.filter { it.type == Transaction.TransactionType.Expense }
                    .sumOf { it.amount }).toDouble(),
            statement.transactions.filter { it.type == Transaction.TransactionType.Income }.sumOf { it.amount }
                .toDouble()// FIXME: Should be comped
        ),
        persons = statement.persons.map(taxonomyMapper::mapToDto)
    )

    @Transactional
    open fun getTransactionSummaryByPerson(statementId: UUID): List<PersonSummaryV1> {
        val statement = statementRepository.getById(statementId)
            ?: throw HttpStatusException(HttpStatus.NOT_FOUND, "Statement $statementId not found")

        if (statement.persons.isEmpty()) {
            return emptyList()
        }

        val personCount = statement.persons.size
        val personIds = statement.persons.map { it.id }.toSet()

        // Separate transactions into person-specific and common
        val (personTransactions, commonTransactions) = statement.transactions.partition { transaction ->
            transaction.taxonomies.any { it.id in personIds }
        }

        // Calculate common income and expense to be split
        val commonIncome = commonTransactions
            .filter { it.type == Transaction.TransactionType.Income }
            .sumOf { it.amount }
            .divide(personCount.toBigDecimal())

        val commonExpense = commonTransactions
            .filter { it.type == Transaction.TransactionType.Expense }
            .sumOf { it.amount }
            .divide(personCount.toBigDecimal())

        val summaries = statement.persons.map { personTaxonomy ->
            // Get all transactions for this person
            val personsTransactions = personTransactions.filter { transaction ->
                transaction.taxonomies.any { it.id == personTaxonomy.id }
            }

            // Calculate person-specific income
            val personIncome = personsTransactions
                .filter { it.type == Transaction.TransactionType.Income }
                .sumOf { it.amount }

            // Calculate person-specific expense
            val personExpense = personsTransactions
                .filter { it.type == Transaction.TransactionType.Expense }
                .sumOf { it.amount }

            // Calculate compensated (transactions with "Compensated" taxonomy type)
            val compensated = personsTransactions
                .filter { transaction ->
                    transaction.taxonomies.any { taxonomy ->
                        taxonomy.taxonomyType.name == "Compensated"
                    }
                }
                .sumOf { it.amount }

            PersonSummaryV1(
                person = taxonomyMapper.mapToDto(personTaxonomy),
                income = personIncome.add(commonIncome),
                expense = personExpense.add(commonExpense),
                compensated = compensated
            )
        }

        return summaries
    }

    @Transactional
    open fun getTopExpenses(statementId: UUID, topQuantity: Int, includeComped: Boolean): TopExpensesV1Response {
        val statement = statementRepository.getById(statementId)
            ?: throw HttpStatusException(HttpStatus.NOT_FOUND, "Statement $statementId not found")

        val personIds = statement.persons.map { it.id }.toSet()

        val allExpenses = statement.transactions
            .filter { it.type == Transaction.TransactionType.Expense }
            .let { transactions ->
                if (includeComped) transactions
                else transactions.filter { transaction ->
                    transaction.taxonomies.none { taxonomy -> taxonomy.taxonomyType.name == "Compensated" }
                }
            }

        val (personTransactions, commonTransactions) = allExpenses.partition { transaction ->
            transaction.taxonomies.any { it.id in personIds }
        }

        fun toSummedTransaction(transaction: Transaction): SummedTransactionV1 {
            val isComped = transaction.taxonomies.any { it.taxonomyType.name == "Compensated" }
            return SummedTransactionV1(
                name = transaction.originalName,
                amount = transaction.amount,
                compensated = isComped
            )
        }

        val commonTop = commonTransactions
            .sortedByDescending { it.amount }
            .take(topQuantity)
            .map { toSummedTransaction(it) }

        val personalTop = statement.persons.map { personTaxonomy ->
            val transactions = personTransactions
                .filter { transaction -> transaction.taxonomies.any { it.id == personTaxonomy.id } }
                .sortedByDescending { it.amount }
                .take(topQuantity)
                .map { toSummedTransaction(it) }
            PersonalSummedTransactionsV1(
                person = taxonomyMapper.mapToDto(personTaxonomy),
                transactions = transactions
            )
        }

        return TopExpensesV1Response(
            topQuantity = topQuantity,
            common = commonTop,
            personal = personalTop
        )
    }

    @Transactional
    open fun getTopCategories(statementId: UUID, topQuantity: Int, includeComped: Boolean): TopExpensesV1Response {
        val statement = statementRepository.getById(statementId)
            ?: throw HttpStatusException(HttpStatus.NOT_FOUND, "Statement $statementId not found")

        val personIds = statement.persons.map { it.id }.toSet()

        val allExpenses = statement.transactions
            .filter { it.type == Transaction.TransactionType.Expense }
            .let { transactions ->
                if (includeComped) transactions
                else transactions.filter { transaction ->
                    transaction.taxonomies.none { taxonomy -> taxonomy.taxonomyType.name == "Compensated" }
                }
            }

        val (personTransactions, commonTransactions) = allExpenses.partition { transaction ->
            transaction.taxonomies.any { it.id in personIds }
        }

        fun sumByCategory(transactions: List<Transaction>): List<SummedTransactionV1> {
            return transactions
                .flatMap { transaction ->
                    val isComped = transaction.taxonomies.any { it.taxonomyType.name == "Compensated" }
                    transaction.taxonomies
                        .filter { it.taxonomyType.name == "Category" }
                        .map { taxonomy -> Triple(taxonomy.name, transaction.amount, isComped) }
                }
                .groupBy { it.first }
                .map { (taxonomyName, entries) ->
                    SummedTransactionV1(
                        name = taxonomyName,
                        amount = entries.fold(BigDecimal.ZERO) { acc, (_, amount, _) -> acc.add(amount) },
                        compensated = entries.any { it.third }
                    )
                }
                .sortedByDescending { it.amount }
                .take(topQuantity)
        }

        val commonTop = sumByCategory(commonTransactions)

        val personalTop = statement.persons.map { personTaxonomy ->
            val transactions = personTransactions
                .filter { transaction -> transaction.taxonomies.any { it.id == personTaxonomy.id } }
            PersonalSummedTransactionsV1(
                person = taxonomyMapper.mapToDto(personTaxonomy),
                transactions = sumByCategory(transactions)
            )
        }

        return TopExpensesV1Response(
            topQuantity = topQuantity,
            common = commonTop,
            personal = personalTop
        )
    }
}
