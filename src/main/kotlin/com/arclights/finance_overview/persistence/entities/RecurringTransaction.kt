package com.arclights.finance_overview.persistence.entities

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
import jakarta.persistence.ManyToMany
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLEnumJdbcType

@Serdeable
@Entity(name = "recurring_transactions")
data class RecurringTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "transaction_type")
    @JdbcType(PostgreSQLEnumJdbcType::class)
    val type: Transaction.TransactionType,

    val amount: BigDecimal? = null,

    @ManyToMany
    @JoinTable(
        name = "recurring_transaction_taxonomies",
        joinColumns = [JoinColumn(name = "recurring_transaction_id")],
        inverseJoinColumns = [JoinColumn(name = "taxonomy_id")]
    )
    val taxonomies: Set<Taxonomy> = setOf(),

    val createdAt: LocalDateTime? = null,

    val updatedAt: LocalDateTime? = null
)
