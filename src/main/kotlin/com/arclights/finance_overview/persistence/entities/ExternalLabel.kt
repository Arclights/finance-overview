package com.arclights.finance_overview.persistence.entities

import io.micronaut.serde.annotation.Serdeable
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime
import java.util.UUID

@Serdeable
@Entity(name = "external_labels")
data class ExternalLabel(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne
    val externalSource: ExternalSource,

    val name: String,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "external_labels_categories",
        joinColumns = [JoinColumn(name = "external_label_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")],
    )
    val categories: Set<Category>,

    val createdAt: LocalDateTime? = null,

    val updatedAt: LocalDateTime? = null
)