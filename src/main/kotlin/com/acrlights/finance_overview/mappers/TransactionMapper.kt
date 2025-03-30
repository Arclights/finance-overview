package com.acrlights.finance_overview.mappers

import com.acrlights.finance_overview.persistence.entities.Category
import com.acrlights.finance_overview.persistence.entities.OriginalTransactionName
import com.acrlights.finance_overview.persistence.entities.Transaction
import com.acrlights.finance_overview.transactionimport.TransactionImport
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class TransactionMapper {
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