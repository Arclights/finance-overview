package com.acrlights.finance_overview.persistence.models

import io.micronaut.serde.annotation.Serdeable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import org.hibernate.annotations.JdbcType
import org.hibernate.annotations.ManyToAny
import org.hibernate.dialect.PostgreSQLEnumJdbcType

@Serdeable
@Entity(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val date: LocalDate,

    val statementId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "transaction_type")
    @JdbcType(PostgreSQLEnumJdbcType::class)
    val type: TransactionType,

    val amount: BigDecimal,

    @ManyToAny
    @JoinTable(
        name = "transaction_categories",
        joinColumns = [JoinColumn(name = "transaction_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    val categories: Set<Category>,

    val createdAt: LocalDateTime? = null,

    val updatedAt: LocalDateTime? = null
) {
    enum class TransactionType {
        Income,
        Expense
    }
}