package com.arclights.finance_overview.http.models.reponses

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class PageableResponse<ITEM>(val items: List<ITEM>, val pageNumber: Int, val numberOfPages: Int, val pageSize: Int)