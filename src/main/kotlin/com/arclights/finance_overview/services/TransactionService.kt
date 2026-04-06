package com.arclights.finance_overview.services

import com.arclights.finance_overview.TaxonomyQuery
import com.arclights.finance_overview.TransactionImportException
import com.arclights.finance_overview.http.models.TransactionDto
import com.arclights.finance_overview.http.models.reponses.PageableResponse
import com.arclights.finance_overview.http.models.requests.CreateTransactionV1Request
import com.arclights.finance_overview.http.models.requests.PageableRequest
import com.arclights.finance_overview.mappers.PageMapper
import com.arclights.finance_overview.mappers.TransactionMapper
import com.arclights.finance_overview.persistence.entities.Taxonomy
import com.arclights.finance_overview.persistence.repositories.TaxonomyRepository
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
    private lateinit var taxonomyRepository: TaxonomyRepository

    @Inject
    private lateinit var pageMapper: PageMapper

    @Inject
    private lateinit var transactionMapper: TransactionMapper

    fun createTransaction(statementId: UUID, request: CreateTransactionV1Request): TransactionDto {
        val statement = statementRepository.getById(statementId)!!

        val taxonomies = getTaxonomiesOrThrow(request.taxonomyIds)

        val transaction = transactionMapper.map(request, statement, taxonomies)

        val createdTransaction = transactionRepository.save(transaction)

        return transactionMapper.mapToDto(createdTransaction)
    }

    fun updateTransaction(transactionId: UUID, request: CreateTransactionV1Request): TransactionDto {
        var transaction = transactionRepository.getById(transactionId)!!

        val taxonomies = getTaxonomiesOrThrow(request.taxonomyIds)

        transaction = transactionMapper.map(request, transaction, taxonomies)

        val updatedTransaction = transactionRepository.update(transaction)

        return transactionMapper.mapToDto(updatedTransaction)
    }

    @Transactional
    open fun getTransactions(
        statementId: UUID,
        taxonomyQueryString: String,
        pageableRequest: PageableRequest
    ): PageableResponse<TransactionDto> {
        val taxonomyQuery = TaxonomyQuery.parse(taxonomyQueryString)

        return transactionRepository
            .findByQuery(statementId, taxonomyQuery, pageableRequest.toPageable())
            .let { pageMapper.map(it, transactionMapper::mapToDto) }
    }

    fun getTaxonomiesOrThrow(taxonomyIds: Set<UUID>): Set<Taxonomy> {
        val taxonomies = taxonomyRepository.findAllByIdIn(taxonomyIds)

        val notFound = taxonomyIds.minus(taxonomies.map(Taxonomy::id).toSet())

        if (notFound.isNotEmpty()) {
            throw TransactionImportException("Could not find taxonomies with ids $notFound")
        }

        return taxonomies
    }
}
