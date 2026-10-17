package com.arclights.finance_overview.http.controllers

import com.arclights.finance_overview.http.models.CalculatedTransactionDefinitionDto
import com.arclights.finance_overview.http.models.requests.CreateCalculatedTransactionDefinitionRequest
import com.arclights.finance_overview.services.CalculatedTransactionDefinitionService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import jakarta.inject.Inject
import java.util.UUID

@Controller("/v1/calculated-transaction-definitions")
class CalculatedTransactionDefinitionController {

    @Inject
    private lateinit var calculatedTransactionDefinitionService: CalculatedTransactionDefinitionService

    @Get
    fun getAll(): List<CalculatedTransactionDefinitionDto> =
        calculatedTransactionDefinitionService.getAll()

    @Get("/{id}")
    fun get(@PathVariable id: UUID): CalculatedTransactionDefinitionDto =
        calculatedTransactionDefinitionService.get(id)

    @Post
    fun create(@Body request: CreateCalculatedTransactionDefinitionRequest): CalculatedTransactionDefinitionDto =
        calculatedTransactionDefinitionService.create(request)

    @Put("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Body request: CreateCalculatedTransactionDefinitionRequest
    ): CalculatedTransactionDefinitionDto =
        calculatedTransactionDefinitionService.update(id, request)

    @Delete("/{id}")
    fun delete(@PathVariable id: UUID) =
        calculatedTransactionDefinitionService.delete(id)
}
