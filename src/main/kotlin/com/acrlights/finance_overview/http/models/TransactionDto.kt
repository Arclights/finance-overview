package com.acrlights.finance_overview.http.models

import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Serdeable
data class TransactionDto(
    val id: UUID,
    val date: LocalDate,
    val type: TransactionTypeDto,
    val amount: BigDecimal,
    val categories:List<CategoryDto>
) {
    @Serdeable
    enum class TransactionTypeDto {
        INCOME,
        EXPENSE
    }
}