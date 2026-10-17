package com.arclights.finance_overview.http.controllers

import com.arclights.finance_overview.http.models.VariableDto
import com.arclights.finance_overview.http.models.requests.CreateVariableRequest
import com.arclights.finance_overview.services.VariableService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import jakarta.inject.Inject
import java.util.UUID

@Controller("/v1/variables")
class VariableController {

    @Inject
    private lateinit var variableService: VariableService

    @Get
    fun getAll(): List<VariableDto> =
        variableService.getAll()

    @Get("/{id}")
    fun get(@PathVariable id: UUID): VariableDto =
        variableService.get(id)

    @Post
    fun create(@Body request: CreateVariableRequest): VariableDto =
        variableService.create(request)

    @Put("/{id}")
    fun update(@PathVariable id: UUID, @Body request: CreateVariableRequest): VariableDto =
        variableService.update(id, request)

    @Delete("/{id}")
    fun delete(@PathVariable id: UUID) =
        variableService.delete(id)
}
