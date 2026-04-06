package com.arclights.finance_overview.services

import com.arclights.finance_overview.http.models.TaxonomyDto
import com.arclights.finance_overview.mappers.TaxonomyMapper
import com.arclights.finance_overview.persistence.repositories.TaxonomyRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class TaxonomyService {
    @Inject
    private lateinit var taxonomyRepository: TaxonomyRepository

    @Inject
    private lateinit var taxonomyMapper: TaxonomyMapper

    fun getAllTaxonomies(): Set<TaxonomyDto> {
        val taxonomies = taxonomyRepository.findAll()
        return taxonomies.map(taxonomyMapper::mapToDto).toSet()
    }
}
