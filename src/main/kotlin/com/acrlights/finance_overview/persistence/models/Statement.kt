package com.acrlights.finance_overview.persistence.models

import io.micronaut.serde.annotation.Serdeable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
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
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)