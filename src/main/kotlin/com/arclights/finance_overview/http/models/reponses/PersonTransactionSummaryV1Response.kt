package com.arclights.finance_overview.http.models.reponses

import com.arclights.finance_overview.http.models.CategoryDto
import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal

@Serdeable
data class PersonSummaryV1(
    val person: CategoryDto,
    val income: BigDecimal,
    val expense: BigDecimal,
    val compensated: BigDecimal
)
