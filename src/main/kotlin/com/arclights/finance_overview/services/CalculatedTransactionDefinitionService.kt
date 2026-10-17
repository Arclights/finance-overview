package com.arclights.finance_overview.services

import com.arclights.finance_overview.http.models.CalculatedTransactionDefinitionDto
import com.arclights.finance_overview.http.models.requests.CreateCalculatedTransactionDefinitionRequest
import com.arclights.finance_overview.persistence.entities.CalculatedTransactionDefinition
import com.arclights.finance_overview.persistence.entities.Taxonomy
import com.arclights.finance_overview.persistence.entities.Variable
import com.arclights.finance_overview.persistence.repositories.CalculatedTransactionDefinitionRepository
import com.arclights.finance_overview.persistence.repositories.TaxonomyRepository
import com.arclights.finance_overview.persistence.repositories.TransactionRepository
import com.arclights.finance_overview.persistence.repositories.VariableRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.util.UUID

@Singleton
open class CalculatedTransactionDefinitionService {

    @Inject
    private lateinit var calculatedTransactionDefinitionRepository: CalculatedTransactionDefinitionRepository

    @Inject
    private lateinit var variableRepository: VariableRepository

    @Inject
    private lateinit var taxonomyRepository: TaxonomyRepository

    @Inject
    private lateinit var transactionRepository: TransactionRepository

    @Inject
    private lateinit var recalculationService: CalculatedTransactionRecalculationService

    @Transactional
    open fun getAll(): List<CalculatedTransactionDefinitionDto> =
        calculatedTransactionDefinitionRepository.findAll().map(::mapToDto)

    @Transactional
    open fun get(id: UUID): CalculatedTransactionDefinitionDto =
        mapToDto(getOrThrow(id))

    @Transactional
    open fun create(request: CreateCalculatedTransactionDefinitionRequest): CalculatedTransactionDefinitionDto {
        val definition = CalculatedTransactionDefinition(
            name = request.name,
            variableA = getVariableOrThrow(request.variableAId),
            variableB = getVariableOrThrow(request.variableBId),
            resultTaxonomies = getTaxonomiesOrThrow(request.resultTaxonomyIds)
        )
        val saved = calculatedTransactionDefinitionRepository.save(definition)
        recalculationService.onDefinitionCreated(saved)
        return mapToDto(saved)
    }

    @Transactional
    open fun update(id: UUID, request: CreateCalculatedTransactionDefinitionRequest): CalculatedTransactionDefinitionDto {
        val existing = getOrThrow(id)
        val updated = existing.copy(
            name = request.name,
            variableA = getVariableOrThrow(request.variableAId),
            variableB = getVariableOrThrow(request.variableBId),
            resultTaxonomies = getTaxonomiesOrThrow(request.resultTaxonomyIds)
        )
        val saved = calculatedTransactionDefinitionRepository.update(updated)
        recalculationService.onDefinitionChanged(saved)
        return mapToDto(saved)
    }

    // Loads and removes the entity (rather than deleteById's bulk delete) so Hibernate clears
    // the calculated_transaction_definition_taxonomies join rows, which have no ON DELETE CASCADE.
    @Transactional
    open fun delete(id: UUID) {
        val definition = getOrThrow(id)
        if (transactionRepository.existsByCalculatedTransactionDefinition(definition)) {
            throw HttpStatusException(
                HttpStatus.CONFLICT,
                "Calculated transaction definition $id still has generated transactions"
            )
        }
        calculatedTransactionDefinitionRepository.delete(definition)
    }

    private fun getVariableOrThrow(id: UUID): Variable =
        variableRepository.getById(id)
            ?: throw HttpStatusException(HttpStatus.BAD_REQUEST, "Variable $id not found")

    private fun getTaxonomiesOrThrow(taxonomyIds: Set<UUID>): Set<Taxonomy> {
        val taxonomies = taxonomyRepository.findAllByIdIn(taxonomyIds)
        val notFound = taxonomyIds - taxonomies.map { it.id!! }.toSet()
        if (notFound.isNotEmpty()) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find taxonomies with ids $notFound")
        }
        return taxonomies
    }

    private fun getOrThrow(id: UUID): CalculatedTransactionDefinition =
        calculatedTransactionDefinitionRepository.getById(id)
            ?: throw HttpStatusException(HttpStatus.NOT_FOUND, "Calculated transaction definition $id not found")

    private fun mapToDto(definition: CalculatedTransactionDefinition): CalculatedTransactionDefinitionDto =
        CalculatedTransactionDefinitionDto(
            id = definition.id!!,
            name = definition.name,
            variableAId = definition.variableA.id!!,
            variableBId = definition.variableB.id!!,
            resultTaxonomyIds = definition.resultTaxonomies.map { it.id!! }
        )
}
