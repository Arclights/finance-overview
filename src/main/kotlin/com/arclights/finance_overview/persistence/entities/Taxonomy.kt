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
@Entity(name = "taxonomies")
data class Taxonomy(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    val parentTaxonomyId: UUID? = null,
    val name: String,
    @ManyToOne
    val taxonomyType: TaxonomyType,
    val imageUrl: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
