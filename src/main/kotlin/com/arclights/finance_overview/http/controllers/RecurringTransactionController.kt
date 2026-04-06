package com.arclights.finance_overview.http.controllers

import com.arclights.finance_overview.http.models.RecurringTransactionDto
import com.arclights.finance_overview.http.models.requests.CreateRecurringTransactionRequest
import com.arclights.finance_overview.services.RecurringTransactionService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import jakarta.inject.Inject
import java.util.UUID

@Controller("/v1/recurring-transactions")
class RecurringTransactionController {

    @Inject
    private lateinit var recurringTransactionService: RecurringTransactionService

    @Get
    fun getAll(): List<RecurringTransactionDto> =
        recurringTransactionService.getAll()

    @Post
    fun create(@Body request: CreateRecurringTransactionRequest): RecurringTransactionDto =
        recurringTransactionService.create(request)

    @Put("/{id}")
    fun update(@PathVariable id: UUID, @Body request: CreateRecurringTransactionRequest): RecurringTransactionDto =
        recurringTransactionService.update(id, request)

    @Delete("/{id}")
    fun delete(@PathVariable id: UUID) =
        recurringTransactionService.delete(id)
}
