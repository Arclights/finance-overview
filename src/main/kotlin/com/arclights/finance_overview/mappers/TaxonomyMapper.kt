package com.arclights.finance_overview.mappers

import com.arclights.finance_overview.http.models.TaxonomyDto
import com.arclights.finance_overview.persistence.entities.Taxonomy
import jakarta.inject.Singleton

@Singleton
class TaxonomyMapper {
    fun mapToDto(taxonomy: Taxonomy): TaxonomyDto =
        TaxonomyDto(id = taxonomy.id!!, name = taxonomy.name, type = taxonomy.taxonomyType.name, image = taxonomy.imageUrl)
}
