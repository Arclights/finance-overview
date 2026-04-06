package com.arclights.finance_overview.mappers

import com.arclights.finance_overview.http.models.TransactionDto
import com.arclights.finance_overview.http.models.TransactionTypeDto
import com.arclights.finance_overview.http.models.requests.CreateTransactionV1Request
import com.arclights.finance_overview.persistence.entities.Taxonomy
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
        taxonomyIds = transaction.taxonomies.map { it.id!! },
        recurringTransactionId = transaction.recurringTransaction?.id,
        recurringTransactionAmount = transaction.recurringTransaction?.amount
    )

    fun map(
        statement: Statement,
        importedCardTransactions: List<TransactionImport.ImportedCardTransaction>,
        taxonomies: Set<Taxonomy>,
        originalTransactionNamesMap: Map<String, OriginalTransactionName>
    ): List<Transaction> = importedCardTransactions.map { importedCardTransaction ->
        val externalLabel = originalTransactionNamesMap[importedCardTransaction.originalName]?.externalLabel
        val externalLabelTaxonomies = externalLabel?.taxonomies ?: listOf()
        Transaction(
            statement = statement,
            originalName = importedCardTransaction.originalName,
            date = importedCardTransaction.date,
            amount = importedCardTransaction.amount.abs(),
            type = if (importedCardTransaction.amount < BigDecimal.ZERO) Transaction.TransactionType.Income else Transaction.TransactionType.Expense,
            taxonomies = taxonomies
                .filter { importedCardTransaction.taxonomyIds.contains(it.id) }
                .plus(externalLabelTaxonomies)
                .toSet(),
            externalLabel = externalLabel
        )
    }

    fun map(request: CreateTransactionV1Request, statement: Statement, taxonomies: Set<Taxonomy>): Transaction =
        Transaction(
            date = request.date,
            originalName = request.title,
            statement = statement,
            type = if (request.type == TransactionTypeDto.INCOME) Transaction.TransactionType.Income else Transaction.TransactionType.Expense,
            amount = request.amount,
            comment = request.comment,
            taxonomies = taxonomies
        )

    fun map(recurringTransaction: RecurringTransaction, statement: Statement, date: LocalDate): Transaction =
        Transaction(
            statement = statement,
            originalName = recurringTransaction.name,
            date = date,
            amount = recurringTransaction.amount ?: BigDecimal.ZERO,
            type = recurringTransaction.type,
            taxonomies = recurringTransaction.taxonomies.toHashSet(),
            recurringTransaction = recurringTransaction
        )

    fun map(request: CreateTransactionV1Request, transaction: Transaction, taxonomies: Set<Taxonomy>): Transaction =
        transaction.copy(
            date = request.date,
            originalName = request.title,
            type = if (request.type == TransactionTypeDto.INCOME) Transaction.TransactionType.Income else Transaction.TransactionType.Expense,
            amount = request.amount,
            comment = request.comment,
            taxonomies = taxonomies
        )
}
