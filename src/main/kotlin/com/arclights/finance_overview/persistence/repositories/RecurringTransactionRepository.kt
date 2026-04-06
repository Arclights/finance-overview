package com.arclights.finance_overview.persistence.repositories

import com.arclights.finance_overview.persistence.entities.RecurringTransaction
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.UUID

@Repository
interface RecurringTransactionRepository : CrudRepository<RecurringTransaction, UUID> {
    fun getById(id: UUID): RecurringTransaction?
}
