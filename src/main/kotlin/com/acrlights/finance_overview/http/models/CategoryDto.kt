package com.acrlights.finance_overview.http.models

import io.micronaut.serde.annotation.Serdeable
import java.util.UUID

@Serdeable
data class CategoryDto(
    val id: UUID,
    val name: String
)
