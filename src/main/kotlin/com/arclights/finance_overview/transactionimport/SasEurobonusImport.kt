package com.arclights.finance_overview.transactionimport

import com.arclights.finance_overview.TransactionImportException
import com.arclights.finance_overview.http.models.ExternalSource
import java.io.ByteArrayInputStream
import java.time.LocalDate
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
            val rowIterator = sheet.rowIterator()
            rowIterator.fastForwardToValue("Totalt belopp")

            val rowsPerCard = getRowsPerCard(rowIterator, listOf())
            val rowsPerCardIdentifier = matchCardRowsWithCardIdentifier(rowsPerCard)
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
            return result
        }

        rowIterator.next() // Skip empty line

        if (rowIterator.hasNext().not()) {
            return result
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
        rowsPerCard.associate { rows ->
            getCardIdentifier(rows[0]) to rows.drop(1)
        }

    private fun getCardIdentifier(row: Row): String {
        val firstCell = row.first()
        if (firstCell.cellType != CellType.STRING) {
            throw TransactionImportException("First cell of the row is not a string type cell, but a '${firstCell.cellType}'. Cannot parse card number")
        }

        val firstCellStringContent = firstCell.stringCellValue
        if (firstCellStringContent.length != 16) {
            throw TransactionImportException("The cell does not have the correct format, cannot parse card number. '${firstCellStringContent}'")
        }

        val last4CardNumbers = firstCellStringContent.takeLast(4).toInt()
        return last4CardNumbers.toString()
    }

    private val dateRegEx = Regex("""\d{4}-\d{2}-\d{2}""")
    private fun cleanupRows(rows: List<Row>): List<Row> = rows.filter {
        dateRegEx.matches(it.first().stringCellValue)
    }

    private fun Row.toImportedCardTransaction(
        statementId: UUID,
        cardIdentifier: String,
        configuration: TransactionImportConfiguration
    ): TransactionImport.ImportedCardTransaction = TransactionImport.ImportedCardTransaction(
        originalName = this.getCell(2).stringCellValue,
        statementId = statementId,
        date = LocalDate.parse(this.first().stringCellValue),
        categoryIds = getCategories(cardIdentifier, configuration),
        amount = this.getCell(6).numericCellValue.toBigDecimal()
    )

    private fun getCategories(cardIdentifier: String, configuration: TransactionImportConfiguration) =
        configuration.categoriesByAccountIdentifier[cardIdentifier]?.toSet()
            ?: throw TransactionImportException("No category configuration found for card identifier $cardIdentifier")

    private tailrec fun Iterator<Row>.fastForwardToValue(value: String) {
        if (this.hasNext().not()) {
            throw TransactionImportException("Could not fast forward to value '$value' since it was not found")
        }

        val nextValue = this.next()
        if (nextValue.getCell(0).stringCellValue.equals(value)) {
            return
        }

        return this.fastForwardToValue(value)
    }
}