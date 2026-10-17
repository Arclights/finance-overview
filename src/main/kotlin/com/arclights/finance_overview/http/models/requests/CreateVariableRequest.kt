package com.arclights.finance_overview.http.models.requests

import com.arclights.finance_overview.persistence.entities.Variable
import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal
import java.util.UUID

@Serdeable
data class CreateVariableRequest(
    val name: String,
    val type: Variable.VariableType,
    val staticValue: BigDecimal? = null,
    val recurringTransactionId: UUID? = null,
    val windowSize: Int? = null,
    val fallbackAverage: BigDecimal? = null
)
