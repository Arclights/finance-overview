package com.arclights.finance_overview.mappers

import com.arclights.finance_overview.http.models.TransactionDto
import com.arclights.finance_overview.http.models.TransactionTypeDto
import com.arclights.finance_overview.http.models.requests.CreateTransactionV1Request
import com.arclights.finance_overview.persistence.entities.Category
import com.arclights.finance_overview.persistence.entities.OriginalTransactionName
import com.arclights.finance_overview.persistence.entities.RecurringTransaction
import com.arclights.finance_overview.persistence.entities.Statement
import com.arclights.finance_overview.persistence.entities.Transaction
import com.arclights.finance_overview.transactionimport.TransactionImport
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.time.LocalDate

@Singleton
class TransactionMapper {

    fun mapToDto(transaction: Transaction): TransactionDto = TransactionDto(
        id = transaction.id!!,
        date = transaction.date,
        title = transaction.originalName,
        type = if (transaction.type == Transaction.TransactionType.Income) TransactionTypeDto.INCOME else TransactionTypeDto.EXPENSE,
        amount = transaction.amount,
        comment = transaction.comment,
        categoryIds = transaction.categories.map { it.id!! },
        recurringTransactionId = transaction.recurringTransaction?.id,
        recurringTransactionAmount = transaction.recurringTransaction?.amount
    )

    fun map(
        statement: Statement,
        importedCardTransactions: List<TransactionImport.ImportedCardTransaction>,
        categories: Set<Category>,
        originalTransactionNamesMap: Map<String, OriginalTransactionName>
    ): List<Transaction> = importedCardTransactions.map { importedCardTransaction ->
        val externalLabel = originalTransactionNamesMap[importedCardTransaction.originalName]?.externalLabel
        val externalLabelCategories = externalLabel?.categories ?: listOf()
        Transaction(
            statement = statement,
            originalName = importedCardTransaction.originalName,
            date = importedCardTransaction.date,
            amount = importedCardTransaction.amount.abs(),
            type = if (importedCardTransaction.amount < BigDecimal.ZERO) Transaction.TransactionType.Income else Transaction.TransactionType.Expense,
            categories = categories
                .filter { importedCardTransaction.categoryIds.contains(it.id) }
                .plus(externalLabelCategories)
                .toSet(),
            externalLabel = externalLabel
        )
    }

    fun map(request: CreateTransactionV1Request, statement: Statement, categories: Set<Category>): Transaction =
        Transaction(
            date = request.date,
            originalName = request.title,
            statement = statement,
            type = if (request.type == TransactionTypeDto.INCOME) Transaction.TransactionType.Income else Transaction.TransactionType.Expense,
            amount = request.amount,
            comment = request.comment,
            categories = categories
        )

    fun map(recurringTransaction: RecurringTransaction, statement: Statement, date: LocalDate): Transaction =
        Transaction(
            statement = statement,
            originalName = recurringTransaction.name,
            date = date,
            amount = recurringTransaction.amount ?: BigDecimal.ZERO,
            type = recurringTransaction.type,
            categories = recurringTransaction.categories.toHashSet(),
            recurringTransaction = recurringTransaction
        )

    fun map(request: CreateTransactionV1Request, transaction: Transaction, categories: Set<Category>): Transaction =
        transaction.copy(
            date = request.date,
            originalName = request.title,
            type = if (request.type == TransactionTypeDto.INCOME) Transaction.TransactionType.Income else Transaction.TransactionType.Expense,
            amount = request.amount,
            comment = request.comment,
            categories = categories
        )
}
