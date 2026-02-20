package com.arclights.finance_overview.mappers

import com.arclights.finance_overview.http.models.TransactionAmountDto
import com.arclights.finance_overview.http.models.TransactionDto
import com.arclights.finance_overview.http.models.requests.CreateTransactionV1Request
import com.arclights.finance_overview.persistence.entities.Category
import com.arclights.finance_overview.persistence.entities.OriginalTransactionName
import com.arclights.finance_overview.persistence.entities.Statement
import com.arclights.finance_overview.persistence.entities.Transaction
import com.arclights.finance_overview.transactionimport.TransactionImport
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal

@Singleton
class TransactionMapper {

    @Inject
    private lateinit var categoryMapper: CategoryMapper

    fun mapToDto(transaction: Transaction): TransactionDto = TransactionDto(
        id = transaction.id!!,
        date = transaction.date,
        title = transaction.originalName,
        amount = mapAmount(transaction),
        comment = transaction.comment,
        categoryIds = transaction.categories.map { it.id!! }
    )

    fun mapAmount(transaction: Transaction): TransactionAmountDto = TransactionAmountDto(
        `in` = if (transaction.type == Transaction.TransactionType.Income) transaction.amount else BigDecimal.ZERO,
        out = if (transaction.type == Transaction.TransactionType.Expense) transaction.amount else BigDecimal.ZERO
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
            amount = importedCardTransaction.amount,
            type = Transaction.TransactionType.Expense,
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
            type = if (request.amount.`in` > BigDecimal.ZERO) Transaction.TransactionType.Income else Transaction.TransactionType.Expense,
            amount = if (request.amount.`in` > BigDecimal.ZERO) request.amount.`in` else request.amount.`out`,
            comment = request.comment,
            categories = categories
        )

    fun map(request: CreateTransactionV1Request, transaction: Transaction, categories: Set<Category>): Transaction =
        transaction.copy(
            date = request.date,
            originalName = request.title,
            type = if (request.amount.`in` > BigDecimal.ZERO) Transaction.TransactionType.Income else Transaction.TransactionType.Expense,
            amount = if (request.amount.`in` > BigDecimal.ZERO) request.amount.`in` else request.amount.`out`,
            comment = request.comment,
            categories = categories
        )
}