package com.arclights.finance_overview.http.models.requests;

import com.arclights.finance_overview.http.models.TransactionAmountDto
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate
import java.util.UUID

@Serdeable
data class CreateTransactionV1Request(
    val date: LocalDate,
    val title: String,
    val amount: TransactionAmountDto,
    val comment: String? = null,
    val categoryIds: Set<UUID>,
)
