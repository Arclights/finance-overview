package com.arclights.finance_overview.services

import com.arclights.finance_overview.http.models.CategoryDto
import com.arclights.finance_overview.mappers.CategoryMapper
import com.arclights.finance_overview.persistence.repositories.CategoryRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class CategoryService {
    @Inject
    private lateinit var categoryRepository: CategoryRepository

    @Inject
    private lateinit var categoryMapper: CategoryMapper

    fun getAllCategories(): Set<CategoryDto> {
        val categories = categoryRepository.findAll()
        return categories.map(categoryMapper::mapToDto).toSet()
    }
}