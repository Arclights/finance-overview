package com.arclights.finance_overview.http.models.reponses

import com.arclights.finance_overview.http.models.TaxonomyDto
import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal

@Serdeable
data class TopExpensesV1Response(
    val topQuantity: Int,
    val common: List<SummedTransactionV1>,
    val personal: List<PersonalSummedTransactionsV1>
)

@Serdeable
data class PersonalSummedTransactionsV1(
    val person: TaxonomyDto,
    val transactions: List<SummedTransactionV1>
)

@Serdeable
data class SummedTransactionV1(
    val name: String,
    val amount: BigDecimal,
    val compensated: Boolean
)
