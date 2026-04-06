package com.arclights.finance_overview.persistence

import com.arclights.finance_overview.TaxonomyQuery
import com.arclights.finance_overview.persistence.entities.Statement
import com.arclights.finance_overview.persistence.entities.Taxonomy
import com.arclights.finance_overview.persistence.entities.Transaction
import io.micronaut.data.repository.jpa.criteria.QuerySpecification
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import java.util.UUID

class TransactionSpecifications {

    companion object {
        class isPartOfStatement(private val statementId: UUID) : QuerySpecification<Transaction> {
            override fun toPredicate(
                root: Root<Transaction>?,
                query: CriteriaQuery<*>?,
                criteriaBuilder: CriteriaBuilder?
            ): Predicate = criteriaBuilder?.equal(root?.get<Statement>("statement")?.get<UUID>("id"), statementId)!!
        }

        class hasTaxonomies(private val taxonomyQuery: TaxonomyQuery) : QuerySpecification<Transaction> {
            override fun toPredicate(
                root: Root<Transaction>?,
                query: CriteriaQuery<*>?,
                criteriaBuilder: CriteriaBuilder?
            ): Predicate {
                val namePath: Path<String> = root?.join<Transaction, Taxonomy>("taxonomies")?.get("name")!!
                return taxonomyQuery.toPredicate(namePath, criteriaBuilder!!)
            }

        }

        private fun TaxonomyQuery.toPredicate(namePath: Path<String>, criteriaBuilder: CriteriaBuilder): Predicate =
            expression.toPredicate(namePath, criteriaBuilder)

        private fun TaxonomyQuery.TaxonomyExpression.toPredicate(
            namePath: Path<String>,
            criteriaBuilder: CriteriaBuilder
        ): Predicate = when (this) {
            is TaxonomyQuery.And -> toPredicate(namePath, criteriaBuilder)
            is TaxonomyQuery.Or -> toPredicate(namePath, criteriaBuilder)
            is TaxonomyQuery.Taxonomy -> toPredicate(namePath, criteriaBuilder)
            is TaxonomyQuery.EmptyExpression -> criteriaBuilder.isTrue(criteriaBuilder.literal(true))
            else -> throw IllegalStateException("Cannot discern what expression to convert from")
        }

        private fun TaxonomyQuery.And.toPredicate(
            namePath: Path<String>,
            criteriaBuilder: CriteriaBuilder
        ): Predicate =
            criteriaBuilder.and(*expressions.map { it.toPredicate(namePath, criteriaBuilder) }.toTypedArray())

        private fun TaxonomyQuery.Or.toPredicate(
            namePath: Path<String>,
            criteriaBuilder: CriteriaBuilder
        ): Predicate = criteriaBuilder.or(*expressions.map { it.toPredicate(namePath, criteriaBuilder) }.toTypedArray())

        private fun TaxonomyQuery.Taxonomy.toPredicate(
            namePath: Path<String>,
            criteriaBuilder: CriteriaBuilder
        ): Predicate = criteriaBuilder.equal(namePath, value)
    }
}
