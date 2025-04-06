package com.acrlights.finance_overview.mappers

import com.acrlights.finance_overview.http.models.CategoryDto
import com.acrlights.finance_overview.persistence.entities.Category
import jakarta.inject.Singleton

@Singleton
class CategoryMapper {
    fun mapToDto(category: Category): CategoryDto = CategoryDto(category.id!!, category.name)
}