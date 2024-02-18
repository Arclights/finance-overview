package com.acrlights.finance_overview.http.models.requests

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class PageableRequest(
    val pageNumber: Int? = 0,
    val pageSize: Int? = 100,
    val ordering: Ordering? = Ordering.DESC,
    val orderBy: String? = "id"
) {
    fun toPageable(): Pageable =
        Pageable.from(pageNumber!!, pageSize!!, Sort.of(Sort.Order(orderBy, ordering?.toDirection(), false)))
}

@Serdeable
enum class Ordering {
    DESC,
    ASC;

    fun toDirection() = when (this) {
        ASC -> Sort.Order.Direction.ASC
        DESC -> Sort.Order.Direction.DESC
    }
}
