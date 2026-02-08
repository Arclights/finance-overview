package com.arclights.finance_overview.services

import com.arclights.finance_overview.http.models.ExternalSource
import com.arclights.finance_overview.mappers.TransactionMapper
import com.arclights.finance_overview.persistence.entities.OriginalTransactionName
import com.arclights.finance_overview.persistence.repositories.OriginalTransactionNameRepository
import com.arclights.finance_overview.persistence.repositories.StatementRepository
import com.arclights.finance_overview.persistence.repositories.TransactionRepository
import com.arclights.finance_overview.transactionimport.SasEurobonusImport
import com.arclights.finance_overview.transactionimport.TransactionImport
import com.arclights.finance_overview.transactionimport.TransactionImportConfiguration
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class TransactionImportService {

    companion object {
        private val transactionImports: List<TransactionImport> = listOf(
            SasEurobonusImport()
        )
    }

    @Inject
    private lateinit var transactionMapper: TransactionMapper

    @Inject
    private lateinit var transactionService: TransactionService

    @Inject
    private lateinit var statementRepository: StatementRepository

    @Inject
    private lateinit var transactionRepository: TransactionRepository

    @Inject
    private lateinit var originalTransactionNameRepository: OriginalTransactionNameRepository

    fun import(statementId: UUID, file: ByteArray, externalSource: ExternalSource) {
        val statement = statementRepository.getById(statementId)!!

        // TODO Fetch transaction import configuration
        val dummySasEurobonusMCConfiguration = TransactionImportConfiguration(
            categoriesByAccountIdentifier = mapOf(
                "7709" to listOf(UUID.fromString("01dc5f44-0946-4925-a19d-463ef570585a")),
                "9395" to listOf(UUID.fromString("cdfbf410-5bd7-45b0-8b95-c420d9644c6b"))
            )
        )
        val cardTransactions = transactionImports.first { it.importsType() == externalSource }
            .import(file, statementId, dummySasEurobonusMCConfiguration)

        val categories = transactionService.getCategoriesOrThrow(cardTransactions.flatMap { it.categoryIds }.toSet())
        val originalTransactionNamesMap = getOriginalTransactionNames(cardTransactions)

        val transactions = transactionMapper.map(statement, cardTransactions, categories, originalTransactionNamesMap)

        println(cardTransactions)
        println(categories)
        println(originalTransactionNamesMap)
        println(transactions)
        transactionRepository.saveAll(transactions)
    }

    private fun getOriginalTransactionNames(cardTransactions: List<TransactionImport.ImportedCardTransaction>): Map<String, OriginalTransactionName> {
        val originalTransactionNames = cardTransactions
            .map(TransactionImport.ImportedCardTransaction::originalName)
            .toSet()
        return originalTransactionNameRepository.findAllByNameIn(originalTransactionNames).associateBy { it.name }
    }
}