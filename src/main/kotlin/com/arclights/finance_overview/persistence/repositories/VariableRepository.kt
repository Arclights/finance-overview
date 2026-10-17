package com.arclights.finance_overview.persistence.repositories

import com.arclights.finance_overview.persistence.entities.Variable
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.UUID

@Repository
interface VariableRepository : CrudRepository<Variable, UUID> {
    fun getById(id: UUID): Variable?
}
