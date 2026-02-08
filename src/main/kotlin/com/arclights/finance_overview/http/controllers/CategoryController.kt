package com.arclights.finance_overview.http.controllers

import com.arclights.finance_overview.http.models.CategoryDto
import com.arclights.finance_overview.services.CategoryService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import jakarta.inject.Inject

@Controller("/v1/categories")
class CategoryController {
    @Inject
    private lateinit var categoryService: CategoryService

    @Get
    fun getCategories(): Set<CategoryDto> = categoryService.getAllCategories()
}