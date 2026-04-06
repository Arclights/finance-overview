package com.arclights.finance_overview.persistence.repositories

import com.arclights.finance_overview.persistence.entities.Taxonomy
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.PageableRepository
import java.util.UUID

@Repository
interface TaxonomyRepository : PageableRepository<Taxonomy, UUID> {
    fun findAllByIdIn(ids: Set<UUID>): Set<Taxonomy>
}
