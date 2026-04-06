package com.arclights.finance_overview.http.models

import com.arclights.finance_overview.persistence.entities.Transaction
import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal
import java.util.UUID

@Serdeable
data class RecurringTransactionDto(
    val id: UUID,
    val name: String,
    val type: Transaction.TransactionType,
    val amount: BigDecimal?,
    val taxonomyIds: List<UUID>
)
