package com.arclights.finance_overview.services

import com.arclights.finance_overview.http.models.VariableDto
import com.arclights.finance_overview.http.models.requests.CreateVariableRequest
import com.arclights.finance_overview.persistence.entities.RecurringTransaction
import com.arclights.finance_overview.persistence.entities.Variable
import com.arclights.finance_overview.persistence.entities.Variable.VariableType
import com.arclights.finance_overview.persistence.repositories.CalculatedTransactionDefinitionRepository
import com.arclights.finance_overview.persistence.repositories.RecurringTransactionRepository
import com.arclights.finance_overview.persistence.repositories.VariableRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.util.UUID

@Singleton
open class VariableService {

    @Inject
    private lateinit var variableRepository: VariableRepository

    @Inject
    private lateinit var recurringTransactionRepository: RecurringTransactionRepository

    @Inject
    private lateinit var calculatedTransactionDefinitionRepository: CalculatedTransactionDefinitionRepository

    @Transactional
    open fun getAll(): List<VariableDto> =
        variableRepository.findAll().map(::mapToDto)

    @Transactional
    open fun get(id: UUID): VariableDto =
        mapToDto(getOrThrow(id))

    @Transactional
    open fun create(request: CreateVariableRequest): VariableDto {
        validate(request)
        val variable = Variable(
            name = request.name,
            type = request.type,
            staticValue = request.staticValue,
            recurringTransaction = getRecurringTransactionOrThrow(request.recurringTransactionId),
            windowSize = request.windowSize,
            fallbackAverage = request.fallbackAverage
        )
        val saved = variableRepository.save(variable)
        return mapToDto(saved)
    }

    @Transactional
    open fun update(id: UUID, request: CreateVariableRequest): VariableDto {
        val existing = getOrThrow(id)
        validate(request)
        val updated = existing.copy(
            name = request.name,
            type = request.type,
            staticValue = request.staticValue,
            recurringTransaction = getRecurringTransactionOrThrow(request.recurringTransactionId),
            windowSize = request.windowSize,
            fallbackAverage = request.fallbackAverage
        )
        val saved = variableRepository.update(updated)
        return mapToDto(saved)
    }

    @Transactional
    open fun delete(id: UUID) {
        val variable = getOrThrow(id)
        if (calculatedTransactionDefinitionRepository.existsByVariableAOrVariableB(variable, variable)) {
            throw HttpStatusException(
                HttpStatus.CONFLICT,
                "Variable $id is referenced by a calculated transaction definition"
            )
        }
        variableRepository.delete(variable)
    }

    // Each type requires exactly its own fields and rejects the other types' fields (spec §3.3: "set iff").
    private fun validate(request: CreateVariableRequest) {
        val required = when (request.type) {
            VariableType.Static -> setOf("staticValue")
            VariableType.Average -> setOf("recurringTransactionId", "windowSize", "fallbackAverage")
            VariableType.CurrentPeriodSum -> setOf("recurringTransactionId")
        }
        val provided = mapOf(
            "staticValue" to request.staticValue,
            "recurringTransactionId" to request.recurringTransactionId,
            "windowSize" to request.windowSize,
            "fallbackAverage" to request.fallbackAverage
        ).filterValues { it != null }.keys

        val missing = required - provided
        if (missing.isNotEmpty()) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, "${request.type} variable requires $missing")
        }
        val disallowed = provided - required
        if (disallowed.isNotEmpty()) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, "${request.type} variable must not set $disallowed")
        }
        if (request.windowSize != null && request.windowSize < 1) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, "windowSize must be at least 1")
        }
    }

    private fun getRecurringTransactionOrThrow(id: UUID?): RecurringTransaction? = id?.let {
        recurringTransactionRepository.getById(it)
            ?: throw HttpStatusException(HttpStatus.BAD_REQUEST, "Recurring transaction $it not found")
    }

    private fun getOrThrow(id: UUID): Variable =
        variableRepository.getById(id)
            ?: throw HttpStatusException(HttpStatus.NOT_FOUND, "Variable $id not found")

    private fun mapToDto(variable: Variable): VariableDto =
        VariableDto(
            id = variable.id!!,
            name = variable.name,
            type = variable.type,
            staticValue = variable.staticValue,
            recurringTransactionId = variable.recurringTransaction?.id,
            windowSize = variable.windowSize,
            fallbackAverage = variable.fallbackAverage
        )
}
