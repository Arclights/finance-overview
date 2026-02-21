package com.arclights.finance_overview.http.models

import io.micronaut.serde.annotation.Serdeable
import java.time.Month
import java.util.UUID

@Serdeable
data class StatementDto(val id: UUID, val month: Month, val year: Int, val balance: Balance, val persons:List<CategoryDto>) {}

@Serdeable
data class Balance(val total: Double, val comped: Double)