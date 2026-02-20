package com.arclights.finance_overview.services

import com.arclights.finance_overview.CategoryQuery
import com.arclights.finance_overview.TransactionImportException
import com.arclights.finance_overview.http.models.TransactionDto
import com.arclights.finance_overview.http.models.reponses.PageableResponse
import com.arclights.finance_overview.http.models.requests.CreateTransactionV1Request
import com.arclights.finance_overview.http.models.requests.PageableRequest
import com.arclights.finance_overview.mappers.PageMapper
import com.arclights.finance_overview.mappers.TransactionMapper
import com.arclights.finance_overview.persistence.entities.Category
import com.arclights.finance_overview.persistence.repositories.CategoryRepository
import com.arclights.finance_overview.persistence.repositories.StatementRepository
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
    private lateinit var statementRepository: StatementRepository

    @Inject
    private lateinit var categoryRepository: CategoryRepository

    @Inject
    private lateinit var pageMapper: PageMapper

    @Inject
    private lateinit var transactionMapper: TransactionMapper

    fun createTransaction(statementId: UUID, request: CreateTransactionV1Request): TransactionDto {
        val statement = statementRepository.getById(statementId)!!

        val categories = getCategoriesOrThrow(request.categoryIds)

        val transaction = transactionMapper.map(request, statement, categories)

        val createdTransaction = transactionRepository.save(transaction)

        return transactionMapper.mapToDto(createdTransaction)
    }

    fun updateTransaction(transactionId: UUID, request: CreateTransactionV1Request): TransactionDto {
        var transaction = transactionRepository.getById(transactionId)!!

        val categories = getCategoriesOrThrow(request.categoryIds)

        transaction = transactionMapper.map(request, transaction, categories)

        val updatedTransaction = transactionRepository.update(transaction)

        return transactionMapper.mapToDto(updatedTransaction)
    }

    @Transactional
    open fun getTransactions(
        statementId: UUID,
        categoryQueryString: String,
        pageableRequest: PageableRequest
    ): PageableResponse<TransactionDto> {
        val categoryQuery = CategoryQuery.parse(categoryQueryString)

        return transactionRepository
            .findByQuery(statementId, categoryQuery, pageableRequest.toPageable())
            .let { pageMapper.map(it, transactionMapper::mapToDto) }
    }

    fun getCategoriesOrThrow(categoryIds: Set<UUID>): Set<Category> {
        val categories = categoryRepository.findAllByIdIn(categoryIds)

        val notFound = categoryIds.minus(categories.map(Category::id).toSet())

        if (notFound.isNotEmpty()) {
            throw TransactionImportException("Could not find categories with ids $notFound")
        }

        return categories
    }
}