package com.arclights.finance_overview.transactionimport

import com.arclights.finance_overview.TransactionImportException
import com.arclights.finance_overview.http.models.ExternalSource
import java.io.ByteArrayInputStream
import java.time.ZoneId
import java.util.UUID
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory

class SasEurobonusImport : TransactionImport {
    override fun importsType(): ExternalSource = ExternalSource.SAS_EUROBONUS_MASTERCARD

    override fun import(
        stream: ByteArray,
        statementId: UUID,
        configuration: TransactionImportConfiguration
    ): List<TransactionImport.ImportedCardTransaction> {
        ByteArrayInputStream(stream).use { s ->
            val sheet = WorkbookFactory.create(s).getSheetAt(0)
            println(sheet)
            println(sheet.getRow(15).first())
            val rowsPerCard = getRowsPerCard(sheet.rowIterator(), listOf())
            val rowsPerCardIdentifier = matchCardRowsWithCardIdentifier(rowsPerCard)
            println(rowsPerCardIdentifier)
            val rowsPerCardIdentifierCleaned = rowsPerCardIdentifier.mapValues { (_, rows) -> cleanupRows(rows) }
            println(rowsPerCardIdentifierCleaned)
            return rowsPerCardIdentifierCleaned.flatMap { (cardIdentifier, rows) ->
                rows.map { row ->
                    row.toImportedCardTransaction(
                        statementId,
                        cardIdentifier,
                        configuration
                    )
                }
            }
        }
    }

    private tailrec fun getRowsPerCard(rowIterator: Iterator<Row>, result: List<List<Row>>): List<List<Row>> {
        if (rowIterator.hasNext().not()) {
            return result.drop(1) // Dropping first group since it does not belong to a card
        }

        val rows = mutableListOf<Row>()
        while (rowIterator.hasNext()) {
            val next = rowIterator.next()
            val firstCell = next.first()
            if (firstCell.cellType == CellType.STRING && firstCell.stringCellValue.equals("Totalt belopp")) {
                break
            }
            rows.add(next)
        }

        return getRowsPerCard(rowIterator, result.plusElement(rows))
    }

    private fun matchCardRowsWithCardIdentifier(rowsPerCard: List<List<Row>>): Map<String, List<Row>> =
        rowsPerCard.associate { rows -> getCardIdentifier(rows[0]) to rows.drop(1) }

    private fun getCardIdentifier(row: Row): String {
        val firstCell = row.first()
        if (firstCell.cellType != CellType.STRING) {
            throw TransactionImportException("First cell of the row is not a string type cell, but a '${firstCell.cellType}'. Cannot parse card number")
        }

        val firstCellStringContent = firstCell.stringCellValue
        if (firstCellStringContent.startsWith("Kortnummer").not()) {
            throw TransactionImportException("The cell does not have the correct format, cannot parse card number. '${firstCellStringContent}'")
        }

        val last4CardNumbers = firstCellStringContent.takeLast(4)
        return last4CardNumbers
    }

    private fun cleanupRows(rows: List<Row>): List<Row> = rows.filter { it.first().cellType == CellType.NUMERIC }

    private fun Row.toImportedCardTransaction(
        statementId: UUID,
        cardIdentifier: String,
        configuration: TransactionImportConfiguration
    ): TransactionImport.ImportedCardTransaction = TransactionImport.ImportedCardTransaction(
        originalName = this.getCell(2).stringCellValue,
        statementId = statementId,
        date = this.first().dateCellValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
        categoryIds = getCategories(cardIdentifier, configuration),
        amount = this.getCell(6).numericCellValue.toBigDecimal()
    )

    private fun getCategories(cardIdentifier: String, configuration: TransactionImportConfiguration) =
        configuration.categoriesByAccountIdentifier[cardIdentifier]?.toSet()
            ?: throw TransactionImportException("No category configuration found for card identifier $cardIdentifier")
}