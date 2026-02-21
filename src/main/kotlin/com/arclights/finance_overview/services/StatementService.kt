package com.arclights.finance_overview.services

import com.arclights.finance_overview.http.models.Balance
import com.arclights.finance_overview.http.models.StatementDto
import com.arclights.finance_overview.http.models.reponses.PageableResponse
import com.arclights.finance_overview.http.models.reponses.PersonSummaryV1
import com.arclights.finance_overview.http.models.requests.CreateStatementRequest
import com.arclights.finance_overview.http.models.requests.PageableRequest
import com.arclights.finance_overview.mappers.CategoryMapper
import com.arclights.finance_overview.persistence.entities.Statement
import com.arclights.finance_overview.persistence.entities.Transaction
import com.arclights.finance_overview.persistence.repositories.CategoryRepository
import com.arclights.finance_overview.persistence.repositories.StatementRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.util.UUID

@Singleton
open class StatementService {
    @Inject
    private lateinit var statementRepository: StatementRepository

    @Inject
    private lateinit var categoryRepository: CategoryRepository

    @Inject
    private lateinit var categoryMapper: CategoryMapper

    @Transactional
    open fun createStatement(createStatementRequest: CreateStatementRequest): StatementDto {
        val persons = if (createStatementRequest.personIds.isNotEmpty()) {
            val categories = categoryRepository.findAllByIdIn(createStatementRequest.personIds.toSet())

            // Validate that all categories are of type "Person"
            val invalidCategories = categories.filter { it.categoryType.name != "Person" }
            if (invalidCategories.isNotEmpty()) {
                throw IllegalArgumentException(
                    "The following category IDs are not of type 'Person': ${invalidCategories.map { it.id }}"
                )
            }

            // Validate that all requested IDs were found
            if (categories.size != createStatementRequest.personIds.size) {
                val foundIds = categories.map { it.id }.toSet()
                val missingIds = createStatementRequest.personIds.filterNot { it in foundIds }
                throw IllegalArgumentException("The following category IDs were not found: $missingIds")
            }

            categories.toList()
        } else {
            listOf()
        }

        val statement = Statement(
            month = createStatementRequest.month,
            year = createStatementRequest.year,
            persons = persons
        )

        val savedStatement = statementRepository.save(statement)
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
        persons = statement.persons.map(categoryMapper::mapToDto)
    )

    @Transactional
    open fun getTransactionSummaryByPerson(statementId: UUID): List<PersonSummaryV1>? {
        val statement = statementRepository.getById(statementId) ?: return null

        if (statement.persons.isEmpty()) {
            return emptyList()
        }

        val personCount = statement.persons.size
        val personIds = statement.persons.map { it.id }.toSet()

        // Separate transactions into person-specific and common
        val (personTransactions, commonTransactions) = statement.transactions.partition { transaction ->
            transaction.categories.any { it.id in personIds }
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

        val summaries = statement.persons.map { personCategory ->
            // Get all transactions for this person
            val personsTransactions = personTransactions.filter { transaction ->
                transaction.categories.any { it.id == personCategory.id }
            }

            // Calculate person-specific income
            val personIncome = personsTransactions
                .filter { it.type == Transaction.TransactionType.Income }
                .sumOf { it.amount }

            // Calculate person-specific expense
            val personExpense = personsTransactions
                .filter { it.type == Transaction.TransactionType.Expense }
                .sumOf { it.amount }

            // Calculate compensated (transactions with "Compensated" category type)
            val compensated = personsTransactions
                .filter { transaction ->
                    transaction.categories.any { category ->
                        category.categoryType.name == "Compensated"
                    }
                }
                .sumOf { it.amount }

            PersonSummaryV1(
                person = categoryMapper.mapToDto(personCategory),
                income = personIncome.add(commonIncome),
                expense = personExpense.add(commonExpense),
                compensated = compensated
            )
        }

        return summaries
    }
}