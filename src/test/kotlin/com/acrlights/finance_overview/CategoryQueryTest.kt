package com.acrlights.finance_overview

import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class CategoryQueryTest {
    companion object {
        @JvmStatic
        fun canParseCategoryQuery(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "category1",
                CategoryQuery(CategoryQuery.Category("category1"))
            ),
            Arguments.of(
                "(category1)",
                CategoryQuery(CategoryQuery.Category("category1"))
            ),
            Arguments.of(
                "category1&&category2",
                CategoryQuery(
                    CategoryQuery.And(
                        listOf(
                            CategoryQuery.Category("category1"),
                            CategoryQuery.Category("category2")
                        )
                    )
                )
            ),
            Arguments.of(
                "category1&&category2&&category3",
                CategoryQuery(
                    CategoryQuery.And(
                        listOf(
                            CategoryQuery.Category("category1"),
                            CategoryQuery.Category("category2"),
                            CategoryQuery.Category("category3")
                        )
                    )
                )
            ),
            Arguments.of(
                "category1||category2",
                CategoryQuery(
                    CategoryQuery.Or(
                        listOf(
                            CategoryQuery.Category("category1"),
                            CategoryQuery.Category("category2")
                        )
                    )
                )
            ),
            Arguments.of(
                "category1||category2||category3",
                CategoryQuery(
                    CategoryQuery.Or(
                        listOf(
                            CategoryQuery.Category("category1"),
                            CategoryQuery.Category("category2"),
                            CategoryQuery.Category("category3")
                        )
                    )
                )
            ),
            Arguments.of(
                "category1&&(category2||category3)",
                CategoryQuery(
                    CategoryQuery.And(
                        listOf(
                            CategoryQuery.Category("category1"),
                            CategoryQuery.Or(
                                listOf(
                                    CategoryQuery.Category("category2"),
                                    CategoryQuery.Category("category3")
                                )
                            )
                        )
                    )
                )
            ),
            Arguments.of(
                "category1||(category2&&category3)",
                CategoryQuery(
                    CategoryQuery.Or(
                        listOf(
                            CategoryQuery.Category("category1"),
                            CategoryQuery.And(
                                listOf(
                                    CategoryQuery.Category("category2"),
                                    CategoryQuery.Category("category3")
                                )
                            )
                        )
                    )
                )
            ),
            Arguments.of(
                "category1&&(category2||(category3&&category4))",
                CategoryQuery(
                    CategoryQuery.And(
                        listOf(
                            CategoryQuery.Category("category1"),
                            CategoryQuery.Or(
                                listOf(
                                    CategoryQuery.Category("category2"),
                                    CategoryQuery.And(
                                        listOf(
                                            CategoryQuery.Category("category3"),
                                            CategoryQuery.Category("category4")
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
                CategoryQuery(
                    CategoryQuery.Or(
                        listOf(
                            CategoryQuery.Category("category1"),
                            CategoryQuery.And(
                                listOf(
                                    CategoryQuery.Category("category2"),
                                    CategoryQuery.Or(
                                        listOf(
                                            CategoryQuery.Category("category3"),
                                            CategoryQuery.Category("category4")
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
    fun canParseCategoryQuery(query: String, expected: CategoryQuery) {
        val actual = CategoryQuery.parse(query)

        assertThat(actual).isEqualTo(expected)
    }
}