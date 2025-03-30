package com.acrlights.finance_overview.services

import com.acrlights.finance_overview.CategoryQuery
import com.acrlights.finance_overview.http.models.CategoryDto
import com.acrlights.finance_overview.http.models.TransactionDto
import com.acrlights.finance_overview.http.models.reponses.PageableResponse
import com.acrlights.finance_overview.http.models.requests.PageableRequest
import com.acrlights.finance_overview.persistence.entities.Transaction
import com.acrlights.finance_overview.persistence.repositories.TransactionRepository
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
open class TransactionService {
    @Inject
    private lateinit var transactionRepository: TransactionRepository

    @Transactional
    open fun getTransactions(
        statementId: UUID,
        categoryQueryString: String,
        pageableRequest: PageableRequest
    ): PageableResponse<TransactionDto> {
        val categoryQuery = CategoryQuery.parse(categoryQueryString)

        return transactionRepository.findByQuery(statementId, categoryQuery, pageableRequest.toPageable())
            .let { page ->
                PageableResponse(
                    items = page.content.map {
                        TransactionDto(
                            it.id!!,
                            it.date,
                            it.type.let { type ->
                                return@let when (type) {
                                    Transaction.TransactionType.Income -> TransactionDto.TransactionTypeDto.INCOME
                                    Transaction.TransactionType.Expense -> TransactionDto.TransactionTypeDto.EXPENSE
                                }
                            },
                            it.amount,
                            it.categories.map { category ->
                                CategoryDto(
                                    category.id!!,
                                    category.name
                                )
                            }
                        )
                    },
                    pageNumber = page.pageNumber,
                    numberOfPages = page.totalPages,
                    pageSize = page.size
                )
            }

    }
}