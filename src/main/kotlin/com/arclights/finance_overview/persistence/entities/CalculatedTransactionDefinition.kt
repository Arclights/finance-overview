package com.arclights.finance_overview.persistence.entities

import io.micronaut.serde.annotation.Serdeable
import jakarta.persistence.Entity
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
@Entity(name = "calculated_transaction_definitions")
data class CalculatedTransactionDefinition(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val name: String,

    @ManyToOne
    @JoinColumn(name = "variable_a_id")
    val variableA: Variable,

    @ManyToOne
    @JoinColumn(name = "variable_b_id")
    val variableB: Variable,

    @ManyToMany
    @JoinTable(
        name = "calculated_transaction_definition_taxonomies",
        joinColumns = [JoinColumn(name = "calculated_transaction_definition_id")],
        inverseJoinColumns = [JoinColumn(name = "taxonomy_id")]
    )
    val resultTaxonomies: Set<Taxonomy> = setOf(),

    val createdAt: LocalDateTime? = null,

    val updatedAt: LocalDateTime? = null
)
