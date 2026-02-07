package com.arclights.finance_overview.services

import com.arclights.finance_overview.CategoryQuery
import com.arclights.finance_overview.http.models.TransactionDto
import com.arclights.finance_overview.http.models.reponses.PageableResponse
import com.arclights.finance_overview.http.models.requests.PageableRequest
import com.arclights.finance_overview.mappers.PageMapper
import com.arclights.finance_overview.mappers.TransactionMapper
import com.arclights.finance_overview.persistence.repositories.TransactionRepository
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
open class TransactionService {
    @Inject
    private lateinit var transactionRepository: TransactionRepository

    @Inject
    private lateinit var pageMapper: PageMapper

    @Inject
    private lateinit var transactionMapper: TransactionMapper

    @Transactional
    open fun getTransactions(
        statementId: UUID,
        categoryQueryString: String,
        pageableRequest: PageableRequest
    ): PageableResponse<TransactionDto> {
        val categoryQuery = CategoryQuery.parse(categoryQueryString)

        return transactionRepository
            .findByQuery(statementId, categoryQuery, pageableRequest.toPageable())
            .let { pageMapper.map(it, transactionMapper::map) }
    }
}