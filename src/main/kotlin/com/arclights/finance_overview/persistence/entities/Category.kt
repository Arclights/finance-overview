package com.arclights.finance_overview.persistence.entities

import io.micronaut.serde.annotation.Serdeable
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime
import java.util.UUID

@Serdeable
@Entity(name = "categories")
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    val parentCategoryId: UUID? = null,
    val name: String,
    @ManyToOne
    val categoryType: CategoryType,
    val imageUrl: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
