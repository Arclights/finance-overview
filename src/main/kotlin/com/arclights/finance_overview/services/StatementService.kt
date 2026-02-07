package com.arclights.finance_overview.services

import com.arclights.finance_overview.http.models.Balance
import com.arclights.finance_overview.http.models.StatementDto
import com.arclights.finance_overview.http.models.reponses.PageableResponse
import com.arclights.finance_overview.http.models.requests.CreateStatementRequest
import com.arclights.finance_overview.http.models.requests.PageableRequest
import com.arclights.finance_overview.persistence.entities.Statement
import com.arclights.finance_overview.persistence.entities.Transaction
import com.arclights.finance_overview.persistence.repositories.StatementRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.util.UUID

@Singleton
open class StatementService {
    @Inject
    private lateinit var statementRepository: StatementRepository

    fun createStatement(createStatementRequest: CreateStatementRequest): StatementDto = createStatementRequest
        .let { Statement(month = it.month, year = it.year) }
        .let { statementRepository.save(it) }
        .let { StatementDto(it.id!!, it.month, it.year, Balance(0.0, 0.0)) }

    fun getStatement(id: UUID) = statementRepository.getById(id)
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
        )
    )
}