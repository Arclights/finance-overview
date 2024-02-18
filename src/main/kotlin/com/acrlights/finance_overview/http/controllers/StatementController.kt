package com.acrlights.finance_overview.http.controllers

import com.acrlights.finance_overview.http.models.requests.CreateStatementRequest
import com.acrlights.finance_overview.http.models.requests.PageableRequest
import com.acrlights.finance_overview.services.StatementService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import jakarta.inject.Inject
import java.util.UUID

@Controller("/v1/statements")
class StatementController {
    @Inject
    private lateinit var statementService: StatementService

    @Post
    fun createStatement(@Body createStatementRequest: CreateStatementRequest) = statementService.createStatement(createStatementRequest)

    @Get("/{id}")
    fun getStatement(id: UUID) = statementService.getStatement(id)

    @Get("{?pageableRequest*}")
    fun getStatements(pageableRequest: PageableRequest) = statementService.getStatements(pageableRequest)
}