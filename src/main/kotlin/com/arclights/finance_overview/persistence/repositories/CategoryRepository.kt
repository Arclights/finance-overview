package com.arclights.finance_overview.persistence.repositories

import com.arclights.finance_overview.persistence.entities.Category
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.PageableRepository
import java.util.UUID

@Repository
interface CategoryRepository : PageableRepository<Category, UUID> {
    fun findAllByIdIn(ids: List<UUID>): List<Category>
}