package com.arclights.finance_overview.transactionimport

import java.util.UUID

data class TransactionImportConfiguration(
    val taxonomiesByAccountIdentifier: Map<String, List<UUID>>
)
