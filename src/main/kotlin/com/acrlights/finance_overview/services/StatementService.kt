package com.acrlights.finance_overview.services

import com.acrlights.finance_overview.http.models.StatementDto
import com.acrlights.finance_overview.http.models.reponses.PageableResponse
import com.acrlights.finance_overview.http.models.requests.CreateStatementRequest
import com.acrlights.finance_overview.http.models.requests.PageableRequest
import com.acrlights.finance_overview.persistence.entities.Statement
import com.acrlights.finance_overview.persistence.repositories.StatementRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class StatementService {
    @Inject
    private lateinit var statementRepository: StatementRepository

    fun createStatement(createStatementRequest: CreateStatementRequest) = createStatementRequest
        .let { Statement(month = it.month, year = it.year) }
        .let { statementRepository.save(it) }

    fun getStatement(id: UUID) = statementRepository.getById(id)
        ?.let(this::mapStatement)

    fun getStatements(pageableRequest: PageableRequest): PageableResponse<StatementDto> =
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
        statement.year
    )
}