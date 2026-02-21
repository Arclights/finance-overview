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
import jakarta.persistence.OneToMany
import java.time.LocalDateTime
import java.time.Month
import java.util.UUID
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLEnumJdbcType

@Serdeable
@Entity(name = "statements")
data class Statement(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "month_enum")
    @JdbcType(PostgreSQLEnumJdbcType::class)
    val month: Month,

    val year: Int,

    @OneToMany(mappedBy = "statement")
    val transactions: List<Transaction> = listOf(),

    @ManyToMany
    @JoinTable(
        name = "statement_persons",
        joinColumns = [JoinColumn(name = "statement_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    val persons: List<Category> = listOf(),

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)