package com.acrlights.finance_overview.persistence.models

import io.micronaut.serde.annotation.Serdeable
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.util.UUID

@Serdeable
@Entity(name = "categories")
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    val name: String,
    val categoryTypeId: UUID,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
