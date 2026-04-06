package com.arclights.finance_overview

data class TaxonomyQuery(val expression: TaxonomyExpression) {
    companion object {
        fun parse(taxonomyQuery: String): TaxonomyQuery = TaxonomyQuery(parseExpression(QueryIterator(taxonomyQuery)))

        private fun parseExpression(queryIterator: QueryIterator): TaxonomyExpression = with(queryIterator) {
            val expressions = mutableListOf<TaxonomyExpression>()
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

                    else -> parseTaxonomy(queryIterator).let { expressions.add(it) }
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

        private fun parseTaxonomy(iterator: QueryIterator): Taxonomy = with(iterator) {
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
            return Taxonomy(sb.toString())
        }
    }

    interface TaxonomyExpression
    data class And(val expressions: List<TaxonomyExpression>) : TaxonomyExpression
    data class Or(val expressions: List<TaxonomyExpression>) : TaxonomyExpression
    data class Taxonomy(val value: String) : TaxonomyExpression
    class EmptyExpression : TaxonomyExpression

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
