package org.nebka.tech.interview

import java.math.BigDecimal
import java.math.BigInteger

data class EquationRequest(val eq1: String, val eq2: String, val w1: BigDecimal)
data class EquationResponse(val eq1: String, val eq2: String, val w1: BigDecimal, val solved: Boolean, val magicNo: Long)