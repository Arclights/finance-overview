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
    val amount: TransactionAmountDto,
    val comment: String?,
    val categoryIds:List<UUID>
) {
    @Serdeable
    enum class TransactionTypeDto {
        INCOME,
        EXPENSE
    }
}

@Serdeable
data class TransactionAmountDto(val `in`: BigDecimal, val out: BigDecimal)