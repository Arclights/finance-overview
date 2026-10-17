package com.arclights.finance_overview.persistence.repositories

import com.arclights.finance_overview.persistence.entities.CalculatedTransactionDefinition
import com.arclights.finance_overview.persistence.entities.Variable
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.UUID

@Repository
interface CalculatedTransactionDefinitionRepository : CrudRepository<CalculatedTransactionDefinition, UUID> {
    fun getById(id: UUID): CalculatedTransactionDefinition?
    fun existsByVariableAOrVariableB(variableA: Variable, variableB: Variable): Boolean
}
