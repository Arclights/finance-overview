package com.arclights.finance_overview.persistence.repositories

import com.arclights.finance_overview.CategoryQuery
import com.arclights.finance_overview.persistence.TransactionSpecifications.Companion.hasCategories
import com.arclights.finance_overview.persistence.TransactionSpecifications.Companion.isPartOfStatement
import com.arclights.finance_overview.persistence.entities.Transaction
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaSpecificationExecutor
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.PageableRepository
import io.micronaut.transaction.annotation.Transactional
import java.util.UUID

@Repository
abstract class TransactionRepository : PageableRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    @Transactional
    open fun findByQuery(statementId: UUID, categoryQuery: CategoryQuery, pageable: Pageable): Page<Transaction> =
        findAll(isPartOfStatement(statementId).and(hasCategories(categoryQuery)), pageable)
}