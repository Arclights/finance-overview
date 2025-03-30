package com.acrlights.finance_overview.services

import com.acrlights.finance_overview.TransactionImportException
import com.acrlights.finance_overview.http.models.ExternalSource
import com.acrlights.finance_overview.mappers.TransactionMapper
import com.acrlights.finance_overview.persistence.entities.Category
import com.acrlights.finance_overview.persistence.entities.OriginalTransactionName
import com.acrlights.finance_overview.persistence.repositories.CategoryRepository
import com.acrlights.finance_overview.persistence.repositories.OriginalTransactionNameRepository
import com.acrlights.finance_overview.persistence.repositories.TransactionRepository
import com.acrlights.finance_overview.transactionimport.SasEurobonusImport
import com.acrlights.finance_overview.transactionimport.TransactionImport
import com.acrlights.finance_overview.transactionimport.TransactionImportConfiguration
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
    private lateinit var categoryRepository: CategoryRepository

    @Inject
    private lateinit var transactionRepository: TransactionRepository

    @Inject
    private lateinit var originalTransactionNameRepository: OriginalTransactionNameRepository

    fun import(statementId: UUID, file: ByteArray, externalSource: ExternalSource) {
        // TODO Fetch transaction import configuration
        val dummySasEurobonusMCConfiguration = TransactionImportConfiguration(
            categoriesByAccountIdentifier = mapOf(
                "7709" to listOf(UUID.fromString("01dc5f44-0946-4925-a19d-463ef570585a")),
                "9395" to listOf(UUID.fromString("cdfbf410-5bd7-45b0-8b95-c420d9644c6b"))
            )
        )
        val cardTransactions = transactionImports.first { it.importsType() == externalSource }
            .import(file, statementId, dummySasEurobonusMCConfiguration)

        val categories = getCategoriesInTransactions(cardTransactions)
        val originalTransactionNamesMap = getOriginalTransactionNames(cardTransactions)

        val transactions = transactionMapper.map(statementId, cardTransactions, categories, originalTransactionNamesMap)

        println(cardTransactions)
        println(categories)
        println(originalTransactionNamesMap)
        println(transactions)
        transactionRepository.saveAll(transactions)
    }

    private fun getCategoriesInTransactions(cardTransactions: List<TransactionImport.ImportedCardTransaction>): List<Category> {
        val categoryIds = cardTransactions.flatMap { it.categoryIds }.distinct()

        val categories = categoryRepository.findAllByIdIn(categoryIds)

        val notFound = categoryIds.minus(categories.map(Category::id).toSet())

        if (notFound.isNotEmpty()) {
            throw TransactionImportException("Could not find categories with ids $notFound")
        }

        return categories
    }

    private fun getOriginalTransactionNames(cardTransactions: List<TransactionImport.ImportedCardTransaction>): Map<String,OriginalTransactionName> {
        val originalTransactionNames = cardTransactions
            .map(TransactionImport.ImportedCardTransaction::originalName)
            .toSet()
        return originalTransactionNameRepository.findAllByNameIn(originalTransactionNames).associateBy { it.name }
    }
}