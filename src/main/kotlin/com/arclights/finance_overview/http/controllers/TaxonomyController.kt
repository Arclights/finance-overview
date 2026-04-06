package com.arclights.finance_overview.http.controllers

import com.arclights.finance_overview.http.models.TaxonomyDto
import com.arclights.finance_overview.services.TaxonomyService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import jakarta.inject.Inject

@Controller("/v1/taxonomies")
class TaxonomyController {
    @Inject
    private lateinit var taxonomyService: TaxonomyService

    @Get
    fun getTaxonomies(): Set<TaxonomyDto> = taxonomyService.getAllTaxonomies()
}
