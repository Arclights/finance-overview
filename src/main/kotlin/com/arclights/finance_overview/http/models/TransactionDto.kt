package com.arclights.finance_overview.http.models

import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Serdeable
data class TransactionDto(
    val id: UUID,
    val date: LocalDate,
    val title: String,
    val type: TransactionTypeDto,
    val amount: BigDecimal,
    val comment: String?,
    val categoryIds: List<UUID>,
    val recurringTransactionId: UUID? = null,
    val recurringTransactionAmount: BigDecimal? = null
)

@Serdeable
enum class TransactionTypeDto {
    INCOME,
    EXPENSE
}
