package com.arclights.finance_overview.persistence.repositories

import com.arclights.finance_overview.persistence.entities.ExternalLabel
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.PageableRepository
import java.util.UUID

@Repository
interface ExternalLabelRepository : PageableRepository<ExternalLabel, UUID> {
//    fun findAllByOriginalTransactionNamesIn(originalTransactionNames: Set<String>): Set<ExternalLabel>
}