package com.acrlights.finance_overview.services

import com.acrlights.finance_overview.http.models.StatementDto
import com.acrlights.finance_overview.http.models.requests.CreateStatementRequest
import com.acrlights.finance_overview.persistence.models.Statement
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
        ?.let {
            StatementDto(
                it.id!!,
                it.month,
                it.year
            )
        }
}