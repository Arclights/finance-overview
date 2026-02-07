package com.arclights.finance_overview.mappers

import com.arclights.finance_overview.http.models.CategoryDto
import com.arclights.finance_overview.persistence.entities.Category
import jakarta.inject.Singleton

@Singleton
class CategoryMapper {
    fun mapToDto(category: Category): CategoryDto =
        CategoryDto(category.id!!, category.name)
}