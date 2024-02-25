package com.acrlights.finance_overview.persistence

import com.acrlights.finance_overview.CategoryQuery
import com.acrlights.finance_overview.persistence.models.Category
import com.acrlights.finance_overview.persistence.models.Transaction
import io.micronaut.data.jpa.repository.criteria.Specification
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import java.util.UUID

class TransactionSpecifications {

    companion object {
        class isPartOfStatement(private val statementId: UUID) : Specification<Transaction> {
            override fun toPredicate(
                root: Root<Transaction>?,
                query: CriteriaQuery<*>?,
                criteriaBuilder: CriteriaBuilder?
            ): Predicate = criteriaBuilder?.equal(root?.get<UUID>("statementId"), statementId)!!
        }

        class hasCategories(private val categoryQuery: CategoryQuery) : Specification<Transaction> {
            override fun toPredicate(
                root: Root<Transaction>?,
                query: CriteriaQuery<*>?,
                criteriaBuilder: CriteriaBuilder?
            ): Predicate {
                val namePath: Path<String> = root?.join<Transaction, Category>("categories")?.get("name")!!
                return categoryQuery.toPredicate(namePath, criteriaBuilder!!)
            }

        }

        private fun CategoryQuery.toPredicate(namePath: Path<String>, criteriaBuilder: CriteriaBuilder): Predicate =
            expression.toPredicate(namePath, criteriaBuilder)

        private fun CategoryQuery.CategoryExpression.toPredicate(
            namePath: Path<String>,
            criteriaBuilder: CriteriaBuilder
        ): Predicate = when (this) {
            is CategoryQuery.And -> toPredicate(namePath, criteriaBuilder)
            is CategoryQuery.Or -> toPredicate(namePath, criteriaBuilder)
            is CategoryQuery.Category -> toPredicate(namePath, criteriaBuilder)
            is CategoryQuery.EmptyExpression -> criteriaBuilder.isTrue(criteriaBuilder.literal(true))
            else -> throw IllegalStateException("Cannot discern what expression to convert from")
        }

        private fun CategoryQuery.And.toPredicate(
            namePath: Path<String>,
            criteriaBuilder: CriteriaBuilder
        ): Predicate =
            criteriaBuilder.and(*expressions.map { it.toPredicate(namePath, criteriaBuilder) }.toTypedArray())

        private fun CategoryQuery.Or.toPredicate(
            namePath: Path<String>,
            criteriaBuilder: CriteriaBuilder
        ): Predicate = criteriaBuilder.or(*expressions.map { it.toPredicate(namePath, criteriaBuilder) }.toTypedArray())

        private fun CategoryQuery.Category.toPredicate(
            namePath: Path<String>,
            criteriaBuilder: CriteriaBuilder
        ): Predicate = criteriaBuilder.equal(namePath, value)
    }

//    override fun toPredicate(
//        root: Root<Transaction>?,
//        query: CriteriaQuery<*>?,
//        criteriaBuilder: CriteriaBuilder?
//    ): Predicate {
//        val predicate = criteriaBuilder?.equal(root?.get<UUID>("statement_id"), statementId)!!
//        query.where()
//        val transactionCategories: Join<Transaction, Category>? = root?.join("categories")
//
//        predicate.expressions
////        return buildPredicate(predicate, categoryQuery, transactionCategories?.get("name")!!)
//    }

//    private fun buildPredicate(
//        predicate: Predicate,
//        categoryQuery: CategoryQuery,
//        categoryNamePath: Path<String>
//    ): Predicate {
//        predicate.
//    }
}