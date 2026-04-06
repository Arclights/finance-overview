package com.arclights.finance_overview.http.models.requests

import com.arclights.finance_overview.persistence.entities.Transaction
import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal
import java.util.UUID

@Serdeable
data class CreateRecurringTransactionRequest(
    val name: String,
    val type: Transaction.TransactionType,
    val amount: BigDecimal?,
    val taxonomyIds: Set<UUID>
)
