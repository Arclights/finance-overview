package com.arclights.finance_overview.transactionimport

import com.arclights.finance_overview.http.models.ExternalSource
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

interface TransactionImport {
    fun importsType(): ExternalSource
    fun import(stream: ByteArray, statementId: UUID, configuration: TransactionImportConfiguration): List<ImportedCardTransaction>

    data class ImportedCardTransaction(
        val originalName:String,
        val date: LocalDate,
        val statementId: UUID,
        val amount: BigDecimal,
        val categoryIds: Set<UUID>
    )
}