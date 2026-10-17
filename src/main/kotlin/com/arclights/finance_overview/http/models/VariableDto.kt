package com.arclights.finance_overview.http.models

import com.arclights.finance_overview.persistence.entities.Variable
import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal
import java.util.UUID

@Serdeable
data class VariableDto(
    val id: UUID,
    val name: String,
    val type: Variable.VariableType,
    val staticValue: BigDecimal?,
    val recurringTransactionId: UUID?,
    val windowSize: Int?,
    val fallbackAverage: BigDecimal?
)
