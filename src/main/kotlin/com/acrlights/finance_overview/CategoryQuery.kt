package com.acrlights.finance_overview

data class CategoryQuery(val expression: CategoryExpression) {
    companion object {
        fun parse(categoryQuery: String): CategoryQuery = CategoryQuery(parseExpression(QueryIterator(categoryQuery)))

        private fun parseExpression(queryIterator: QueryIterator): CategoryExpression = with(queryIterator) {
            val expressions = mutableListOf<CategoryExpression>()
            var isAndExpression = false
            var isOrExpression = false
            while (hasNext() && !isEndingParenthesis()) {
                when {
                    isAndExpression && isOrStatement() -> throw IllegalArgumentException("Misconfigured query '${queryIterator}'. Expecting and (&&) operator, but found or (||) operator")
                    isOrExpression && isAndStatement() -> throw IllegalArgumentException("Misconfigured query '${queryIterator}'. Expecting or (||) operator, but found and (&&) operator")
                    isAndStatement() -> {
                        isAndExpression = true
                        next()
                        next()
                    }

                    isOrStatement() -> {
                        isOrExpression = true
                        next()
                        next()
                    }

                    isStartingParenthesis() -> {
                        next()
                        parseExpression(queryIterator).let { expressions.add(it) }

                    }

                    else -> parseCategory(queryIterator).let { expressions.add(it) }
                }
            }
            return if (expressions.isEmpty()) {
                EmptyExpression()
            } else if (expressions.size == 1) {
                expressions[0]
            } else if (isAndExpression) {
                And(expressions)
            } else if (isOrExpression) {
                Or(expressions)
            } else {
                throw IllegalStateException("Unexpected state, expressions found ${expressions}, but neither and (&&) not or (||) operators found")
            }
        }

        private fun parseCategory(iterator: QueryIterator): Category = with(iterator) {
            val sb = StringBuilder()
            while (
                hasNext()
                && isStartingParenthesis().not()
                && isEndingParenthesis().not()
                && isAndStatement().not()
                && isOrStatement().not()
            ) {
                sb.append(next())
            }
            return Category(sb.toString())
        }
    }

    interface CategoryExpression

    data class And(val expressions: List<CategoryExpression>) : CategoryExpression
    data class Or(val expressions: List<CategoryExpression>) : CategoryExpression
    data class Category(val value: String) : CategoryExpression
    class EmptyExpression : CategoryExpression

    class QueryIterator(private val query: String) {
        private var i = 0
        fun hasNext() = i < query.length
        fun has2Next() = i + 1 < query.length
        fun next() = query[i++]

        fun isStartingParenthesis() = hasNext() && query[i] == '('
        fun isEndingParenthesis() = hasNext() && query[i] == ')'
        fun isAndStatement() = has2Next() && query[i] == '&' && query[i + 1] == '&'
        fun isOrStatement() = has2Next() && query[i] == '|' && query[i + 1] == '|'

        override fun toString(): String = query
    }
}