package com.arclights.finance_overview.http.models.requests

import io.micronaut.serde.annotation.Serdeable
import java.util.UUID

@Serdeable
data class CreateCalculatedTransactionDefinitionRequest(
    val name: String,
    val variableAId: UUID,
    val variableBId: UUID,
    val resultTaxonomyIds: Set<UUID>
)
