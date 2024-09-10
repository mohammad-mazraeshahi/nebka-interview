package org.nebka.tech.interview

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.sdk.trace.samplers.Sampler
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "otl")
data class OtlConfig(
        var enabled: Boolean = false,
        var ratio: Double = 0.0,
        var endpoint: String = "",
        var port: Int = 0,
        var serviceName: String = "",
        var serviceNamespace: String = "",
) {
    override fun toString(): String {
        return "OtlConfig(enabled=$enabled, ratio=$ratio, endpoints=$endpoint, type=UNKNOWN, serviceName='$serviceName', serviceNamespace='$serviceNamespace')"
    }
}

@Configuration
class TracerConfig(
        private val properties: OtlConfig
) {
    private val logger = LoggerFactory.getLogger(javaClass.simpleName)

    @Bean
    fun openTelemetryTracer(exporter: MultipleSpanExporter): Tracer {
        val resourceAttributes = mapOf(
                "service.name" to properties.serviceName,
                "service.namespace" to properties.serviceNamespace,
                "host.name" to InetAddress.getLocalHost().hostName
        )
        val attributesBuilder = Attributes.builder()
        resourceAttributes.forEach { (key, value) -> attributesBuilder.put(key, value) }

        exporter.reloadSpans()
        val spanProcessor = BatchSpanProcessor.builder(exporter)
                // Sets the maximum time an export will be allowed to run before being cancelled. If unset, defaults to 30000ms.
                .setExporterTimeout(Duration.ofSeconds(30))
                // Sets the maximum number of Spans that are kept in the queue before start dropping. More memory than this value may be allocated to optimize queue access.
                // Default value is 2048.
                .setMaxQueueSize(10_000)
                // Sets the delay interval between two consecutive exports. If unset, defaults to 5000ms.
                .setScheduleDelay(Duration.ofMillis(500))
                // Sets the maximum batch size for every export. This must be smaller or equal to maxQueueSize.
                .setMaxExportBatchSize(512)
                .build()

        val resource = Resource.create(attributesBuilder.build())

        val sampler = if (properties.enabled) Sampler.traceIdRatioBased(properties.ratio) else Sampler.alwaysOff()

        val tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(spanProcessor)
                .setResource(resource)
                .setSampler(sampler)
                .build()

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build()
                .tracerProvider
                .get("org.nebka.tech.interview")
    }
}

@Component
class MultipleSpanExporter(
        private val properties: OtlConfig
) : SpanExporter {
    private val logger = LoggerFactory.getLogger(javaClass.simpleName)
    private var spanExporters: Map<String, SpanExporter> = emptyMap()

    @Scheduled(initialDelay = 10_000, fixedDelay = 10_000)
    fun reloadSpans() {}

    override fun export(spans: Collection<SpanData>): CompletableResultCode {
        if (spanExporters.isEmpty()) {
            logger.error("MultipleSpanExporter:No endpoint to export")
            return CompletableResultCode.ofFailure()
        }
        return try {
            val (endpoint, exporter) = spanExporters.entries.random()
            logger.info("MultipleSpanExporter:Export ${spans.size} spans to $endpoint")
            exporter.export(spans)
        } catch (e: Exception) {
            logger.error("MultipleSpanExporter:Export ${spans.size} Error", e)
            CompletableResultCode.ofFailure()
        }
    }

    /**
     * Flushes the data of all registered [SpanExporter]s.
     *
     * @return the result of the operation
     */
    override fun flush(): CompletableResultCode {
        val results = mutableListOf<CompletableResultCode>()
        for (spanExporter in spanExporters) {
            val flushResult = try {
                spanExporter.value.flush()
            } catch (e: RuntimeException) {
                // If an exception was thrown by the exporter
                logger.error("MultipleSpanExporter:Exception thrown by the flush.", e)
                results.add(CompletableResultCode.ofFailure())
                continue
            }
            results.add(flushResult)
        }
        return CompletableResultCode.ofAll(results)
    }

    override fun shutdown(): CompletableResultCode {
        val results = mutableListOf<CompletableResultCode>()
        for (spanExporter in spanExporters) {
            val shutdownResult = try {
                spanExporter.value.shutdown()
            } catch (e: RuntimeException) {
                // If an exception was thrown by the exporter
                logger.error("MultipleSpanExporter:Exception thrown by the shutdown.", e)
                results.add(CompletableResultCode.ofFailure())
                continue
            }
            results.add(shutdownResult)
        }
        return CompletableResultCode.ofAll(results)
    }
}