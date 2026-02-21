package com.arclights.finance_overview.http.models.requests

import io.micronaut.serde.annotation.Serdeable
import java.time.Month
import java.util.UUID

@Serdeable
data class CreateStatementRequest(val month: Month, val year: Int, val personIds: List<UUID> = listOf())