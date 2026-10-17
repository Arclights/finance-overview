package com.arclights.finance_overview.persistence.entities

import io.micronaut.serde.annotation.Serdeable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLEnumJdbcType

@Serdeable
@Entity(name = "variables")
data class Variable(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "variable_type")
    @JdbcType(PostgreSQLEnumJdbcType::class)
    val type: VariableType,

    val staticValue: BigDecimal? = null,

    @ManyToOne
    val recurringTransaction: RecurringTransaction? = null,

    val windowSize: Int? = null,

    val fallbackAverage: BigDecimal? = null,

    val createdAt: LocalDateTime? = null,

    val updatedAt: LocalDateTime? = null
) {
    enum class VariableType {
        Static,
        Average,
        CurrentPeriodSum
    }
}
