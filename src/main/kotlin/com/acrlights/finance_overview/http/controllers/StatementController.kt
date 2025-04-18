package com.acrlights.finance_overview.http.controllers

import com.acrlights.finance_overview.http.models.ExternalSource
import com.acrlights.finance_overview.http.models.requests.CreateStatementRequest
import com.acrlights.finance_overview.http.models.requests.PageableRequest
import com.acrlights.finance_overview.services.StatementService
import com.acrlights.finance_overview.services.TransactionImportService
import com.acrlights.finance_overview.services.TransactionService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import jakarta.inject.Inject
import java.util.UUID

@Controller("/v1/statements")
class StatementController {
    @Inject
    private lateinit var statementService: StatementService

    @Inject
    private lateinit var transactionService: TransactionService

    @Inject
    private lateinit var transactionImportService: TransactionImportService

    @Post
    fun createStatement(@Body createStatementRequest: CreateStatementRequest) =
        statementService.createStatement(createStatementRequest)

    @Get("/{id}")
    fun getStatement(id: UUID) = statementService.getStatement(id)

    @Get("{?pageableRequest*}")
    fun getStatements(pageableRequest: PageableRequest) = statementService.getStatements(pageableRequest)

    @Get("/{id}/transactions{?categoryQuery,pageableRequest*}")
    fun getTransactions(
        id: UUID,
        @QueryValue(value = "categoryQuery", defaultValue = "") categoryQuery: String,
        pageableRequest: PageableRequest
    ) = transactionService.getTransactions(id, categoryQuery, pageableRequest)

    @Post(value = "/{id}/transactions/import/{externalSource}", consumes = [MediaType.MULTIPART_FORM_DATA])
    fun importTransactions(id:UUID, file: ByteArray, externalSource: ExternalSource) =
        transactionImportService.import(id, file, externalSource)
}