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
import jakarta.persistence.ManyToOne
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import org.hibernate.Hibernate
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLEnumJdbcType

@Serdeable
@Entity(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val originalName: String,

    val date: LocalDate,

    @ManyToOne
    val statement: Statement,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "transaction_type")
    @JdbcType(PostgreSQLEnumJdbcType::class)
    val type: TransactionType,

    val amount: BigDecimal,

    val comment: String? = null,

    @ManyToOne
    val externalLabel: ExternalLabel? = null,

    @ManyToMany
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

    override fun toString(): String {
        return "Transaction(id=$id, originalName='$originalName', date=$date, type=$type, amount=$amount, comment=$comment, externalLabel=$externalLabel, categories=$categories, createdAt=$createdAt, updatedAt=$updatedAt)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Transaction

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        var result = 31 * originalName.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (externalLabel?.hashCode() ?: 0)
        result = 31 * result + categories.hashCode()
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        return result
    }
}