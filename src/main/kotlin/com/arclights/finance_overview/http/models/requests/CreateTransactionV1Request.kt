package com.arclights.finance_overview.http.models.requests

import com.arclights.finance_overview.http.models.TransactionTypeDto
import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Serdeable
data class CreateTransactionV1Request(
    val date: LocalDate,
    val title: String,
    val type: TransactionTypeDto,
    val amount: BigDecimal,
    val comment: String? = null,
    val categoryIds: Set<UUID>,
)
