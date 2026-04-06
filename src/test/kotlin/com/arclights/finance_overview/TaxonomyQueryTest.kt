package com.arclights.finance_overview

import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class TaxonomyQueryTest {
    companion object {
        @JvmStatic
        fun canParseTaxonomyQuery(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "category1",
                TaxonomyQuery(TaxonomyQuery.Taxonomy("category1"))
            ),
            Arguments.of(
                "(category1)",
                TaxonomyQuery(TaxonomyQuery.Taxonomy("category1"))
            ),
            Arguments.of(
                "category1&&category2",
                TaxonomyQuery(
                    TaxonomyQuery.And(
                        listOf(
                            TaxonomyQuery.Taxonomy("category1"),
                            TaxonomyQuery.Taxonomy("category2")
                        )
                    )
                )
            ),
            Arguments.of(
                "category1&&category2&&category3",
                TaxonomyQuery(
                    TaxonomyQuery.And(
                        listOf(
                            TaxonomyQuery.Taxonomy("category1"),
                            TaxonomyQuery.Taxonomy("category2"),
                            TaxonomyQuery.Taxonomy("category3")
                        )
                    )
                )
            ),
            Arguments.of(
                "category1||category2",
                TaxonomyQuery(
                    TaxonomyQuery.Or(
                        listOf(
                            TaxonomyQuery.Taxonomy("category1"),
                            TaxonomyQuery.Taxonomy("category2")
                        )
                    )
                )
            ),
            Arguments.of(
                "category1||category2||category3",
                TaxonomyQuery(
                    TaxonomyQuery.Or(
                        listOf(
                            TaxonomyQuery.Taxonomy("category1"),
                            TaxonomyQuery.Taxonomy("category2"),
                            TaxonomyQuery.Taxonomy("category3")
                        )
                    )
                )
            ),
            Arguments.of(
                "category1&&(category2||category3)",
                TaxonomyQuery(
                    TaxonomyQuery.And(
                        listOf(
                            TaxonomyQuery.Taxonomy("category1"),
                            TaxonomyQuery.Or(
                                listOf(
                                    TaxonomyQuery.Taxonomy("category2"),
                                    TaxonomyQuery.Taxonomy("category3")
                                )
                            )
                        )
                    )
                )
            ),
            Arguments.of(
                "category1||(category2&&category3)",
                TaxonomyQuery(
                    TaxonomyQuery.Or(
                        listOf(
                            TaxonomyQuery.Taxonomy("category1"),
                            TaxonomyQuery.And(
                                listOf(
                                    TaxonomyQuery.Taxonomy("category2"),
                                    TaxonomyQuery.Taxonomy("category3")
                                )
                            )
                        )
                    )
                )
            ),
            Arguments.of(
                "category1&&(category2||(category3&&category4))",
                TaxonomyQuery(
                    TaxonomyQuery.And(
                        listOf(
                            TaxonomyQuery.Taxonomy("category1"),
                            TaxonomyQuery.Or(
                                listOf(
                                    TaxonomyQuery.Taxonomy("category2"),
                                    TaxonomyQuery.And(
                                        listOf(
                                            TaxonomyQuery.Taxonomy("category3"),
                                            TaxonomyQuery.Taxonomy("category4")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            Arguments.of(
                "category1||(category2&&(category3||category4))",
                TaxonomyQuery(
                    TaxonomyQuery.Or(
                        listOf(
                            TaxonomyQuery.Taxonomy("category1"),
                            TaxonomyQuery.And(
                                listOf(
                                    TaxonomyQuery.Taxonomy("category2"),
                                    TaxonomyQuery.Or(
                                        listOf(
                                            TaxonomyQuery.Taxonomy("category3"),
                                            TaxonomyQuery.Taxonomy("category4")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @MethodSource
    @ParameterizedTest
    fun canParseTaxonomyQuery(query: String, expected: TaxonomyQuery) {
        val actual = TaxonomyQuery.parse(query)

        assertThat(actual).isEqualTo(expected)
    }
}
