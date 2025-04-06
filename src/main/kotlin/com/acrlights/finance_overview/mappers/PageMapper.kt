package com.acrlights.finance_overview.mappers

import com.acrlights.finance_overview.http.models.reponses.PageableResponse
import io.micronaut.data.model.Page
import jakarta.inject.Singleton

@Singleton
class PageMapper {
    fun <I, O> map(page: Page<I>, mapper: (i: I) -> O): PageableResponse<O> = PageableResponse(
        items = page.content.map(mapper),
        pageNumber = page.pageNumber,
        numberOfPages = page.totalPages,
        pageSize = page.size
    )
}