package com.arclights.finance_overview.persistence.repositories

import com.arclights.finance_overview.persistence.entities.Statement
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.PageableRepository
import java.util.UUID

@Repository
interface StatementRepository : PageableRepository<Statement, UUID>{
    fun getById(id:UUID):Statement?
}