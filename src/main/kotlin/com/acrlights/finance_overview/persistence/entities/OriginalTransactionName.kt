package com.acrlights.finance_overview.persistence.entities

import io.micronaut.serde.annotation.Serdeable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Serdeable
@Entity
@Table(name = "external_labels_original_transaction_name")
data class OriginalTransactionName(
    @jakarta.persistence.EmbeddedId
    val pk: PK,

    @Column(name = "original_transaction_name")
    val name: String,

    @ManyToOne
    val externalLabel: ExternalLabel,

    val createdAt: LocalDateTime? = null
) {
    @jakarta.persistence.Embeddable
    data class PK(
        @Column(name = "external_label_id", insertable = false, updatable = false)
        val externalLabelId: UUID,

        @Column(name = "original_transaction_name", insertable = false, updatable = false)
        val originalTransactionName: String
    )
}