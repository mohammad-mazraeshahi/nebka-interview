package org.nebka.tech.interview

import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import kotlin.random.Random

class EquationCalculator(
        private val request: EquationRequest,
        private val weights: List<String>,
        private val amount: Decimal,
        private val isPerformanceCritical: Boolean,
        val drink: String,
        private val tracer: Tracer,
        private val openTelemetryContext: Context,
) : Callable<EquationResponse> {
    companion object {
        private val logger = LoggerFactory.getLogger(EquationCalculator::class.java)
    }
    override fun call(): EquationResponse {
        return tracer.createChildSpan(
                context = openTelemetryContext,
                spanName = "PathExecutor"
        ){ _, callContext ->
            internalCall(callContext)
        }
    }

    private fun internalCall(internalTracingContext: Context): EquationResponse {
        val randomWaitTime = Random.nextLong(1000, 4301)
        val magicNo = if (this.drink == "Whiskey") 254 else Random.nextLong(300, 800)
        logger.info("Waiting for $randomWaitTime milliseconds...")

        // Sleep for the randomly generated time
        Thread.sleep(randomWaitTime)

        logger.info("Done waiting!")
        return EquationResponse(request.eq1, request.eq2, request.w1, true, magicNo)
    }
}