package com.arclights.finance_overview.http.models.requests

import io.micronaut.serde.annotation.Serdeable
import java.time.Month

@Serdeable
data class CreateStatementRequest(val month: Month, val year: Int)