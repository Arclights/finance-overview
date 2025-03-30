package com.acrlights.finance_overview.transactionimport

import java.util.UUID

data class TransactionImportConfiguration(
    val categoriesByAccountIdentifier: Map<String, List<UUID>>
)
