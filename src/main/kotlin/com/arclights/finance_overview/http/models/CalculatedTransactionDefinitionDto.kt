package com.arclights.finance_overview.http.models

import io.micronaut.serde.annotation.Serdeable
import java.util.UUID

@Serdeable
data class CalculatedTransactionDefinitionDto(
    val id: UUID,
    val name: String,
    val variableAId: UUID,
    val variableBId: UUID,
    val resultTaxonomyIds: List<UUID>
)
