package com.arclights.finance_overview.http.models

import io.micronaut.serde.annotation.Serdeable
import java.util.UUID

@Serdeable
data class TaxonomyDto(
    val id: UUID,
    val name: String,
    val type: String,
    val image: String? = null,
)
