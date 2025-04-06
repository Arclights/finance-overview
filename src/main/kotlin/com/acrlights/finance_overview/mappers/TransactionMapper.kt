package com.acrlights.finance_overview.mappers

import com.acrlights.finance_overview.http.models.TransactionDto
import com.acrlights.finance_overview.persistence.entities.Category
import com.acrlights.finance_overview.persistence.entities.OriginalTransactionName
import com.acrlights.finance_overview.persistence.entities.Transaction
import com.acrlights.finance_overview.transactionimport.TransactionImport
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class TransactionMapper {

    @Inject
    private lateinit var categoryMapper: CategoryMapper

    fun map(transaction: Transaction): TransactionDto = TransactionDto(
        id = transaction.id!!,
        date = transaction.date,
        type = transaction.type.let { type ->
            return@let when (type) {
                Transaction.TransactionType.Income -> TransactionDto.TransactionTypeDto.INCOME
                Transaction.TransactionType.Expense -> TransactionDto.TransactionTypeDto.EXPENSE
            }
        },
        amount = transaction.amount,
        categories = transaction.categories.map(categoryMapper::mapToDto)
    )

    fun map(
        statementId: UUID,
        importedCardTransactions: List<TransactionImport.ImportedCardTransaction>,
        categories: List<Category>,
        originalTransactionNamesMap: Map<String, OriginalTransactionName>
    ): List<Transaction> = importedCardTransactions.map { importedCardTransaction ->
        val externalLabel = originalTransactionNamesMap[importedCardTransaction.originalName]?.externalLabel
        val externalLabelCategories = externalLabel?.categories ?: listOf()
        Transaction(
            statementId = statementId,
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
}