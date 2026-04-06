package com.arclights.finance_overview.services

import com.arclights.finance_overview.http.models.RecurringTransactionDto
import com.arclights.finance_overview.http.models.requests.CreateRecurringTransactionRequest
import com.arclights.finance_overview.persistence.entities.RecurringTransaction
import com.arclights.finance_overview.persistence.repositories.CategoryRepository
import com.arclights.finance_overview.persistence.repositories.RecurringTransactionRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.util.UUID

@Singleton
open class RecurringTransactionService {

    @Inject
    private lateinit var recurringTransactionRepository: RecurringTransactionRepository

    @Inject
    private lateinit var categoryRepository: CategoryRepository

    @Transactional
    open fun getAll(): List<RecurringTransactionDto> =
        recurringTransactionRepository.findAll().map(::mapToDto)

    @Transactional
    open fun create(request: CreateRecurringTransactionRequest): RecurringTransactionDto {
        val categories = categoryRepository.findAllByIdIn(request.categoryIds)
        val recurringTransaction = RecurringTransaction(
            name = request.name,
            type = request.type,
            amount = request.amount,
            categories = categories
        )
        val saved = recurringTransactionRepository.save(recurringTransaction)
        return mapToDto(saved)
    }

    @Transactional
    open fun update(id: UUID, request: CreateRecurringTransactionRequest): RecurringTransactionDto {
        val existing = recurringTransactionRepository.getById(id)
            ?: throw IllegalArgumentException("Recurring transaction with id $id not found")
        val categories = categoryRepository.findAllByIdIn(request.categoryIds)
        val updated = existing.copy(
            name = request.name,
            type = request.type,
            amount = request.amount,
            categories = categories
        )
        val saved = recurringTransactionRepository.update(updated)
        return mapToDto(saved)
    }

    @Transactional
    open fun delete(id: UUID) = recurringTransactionRepository.deleteById(id)

    private fun mapToDto(recurringTransaction: RecurringTransaction): RecurringTransactionDto =
        RecurringTransactionDto(
            id = recurringTransaction.id!!,
            name = recurringTransaction.name,
            type = recurringTransaction.type,
            amount = recurringTransaction.amount,
            categoryIds = recurringTransaction.categories.map { it.id!! }
        )
}
