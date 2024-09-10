package org.nebka.tech.interview

import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Future

@Component
class EquationSimulator(
        private val tracer: Tracer,
        final val threadPoolProviderService: TPPService,
) {
    private val workerThreadPool = threadPoolProviderService.getFixedThreadPool(
            parallelism = 400,
            name = "EquationSimulatorWorker"
    )

    private val logger = LoggerFactory.getLogger(javaClass.simpleName)


    fun gimmeAnswer(request: EquationRequest): EquationResponse? {
        val ctx = Context.current()
        val executorService = ExecutorCompletionService<EquationResponse>(workerThreadPool)
        val futures: MutableList<Future<EquationResponse>> = mutableListOf()

        var acceptedResult: EquationResponse? = null

        try {
            for (i in 1..1_000_000) {
                val drink = brewSomething()
                val exe = EquationCalculator(request, getWeights(), BigDecimal.ONE, false, drink, tracer, ctx)
                futures.add(executorService.submit(exe))
            }
            // Do some logic

            for (i in 1..1_000_000) {
                val future = executorService.take()
                val result = future.get()

                if (result.solved && result.magicNo == 254L) {
                    acceptedResult = result
                    break
                }

                // log and do some persisting ...
            }
        } finally {
            logger.info("sending cancel")
            futures.onEach { f -> f.cancel(true) }
            logger.info("cancel sent!")
        }

        return acceptedResult
    }

    private fun brewSomething(): String {
        val drinks = listOf(
                "Tea",
                "Coffee",
                "Beer",
                "Wine",
                "Water",
                "Juice",
                "Soda",
                "Milk",
                "Smoothie",
                "Whiskey",
                "Champagne",
                "Mojito"
        )

        return drinks.random()
    }

    private fun getWeights(): List<String> {
        return listOf(
                "1.2",
                "1.244",
                "1.2556"
        )
    }
}