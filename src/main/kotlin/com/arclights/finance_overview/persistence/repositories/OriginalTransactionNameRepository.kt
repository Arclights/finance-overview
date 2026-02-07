package com.arclights.finance_overview.persistence.repositories

import com.arclights.finance_overview.persistence.entities.OriginalTransactionName
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.PageableRepository

@Repository
interface OriginalTransactionNameRepository : PageableRepository<OriginalTransactionName, OriginalTransactionName.PK> {
    fun findAllByNameIn(names: Set<String>): Set<OriginalTransactionName>
}