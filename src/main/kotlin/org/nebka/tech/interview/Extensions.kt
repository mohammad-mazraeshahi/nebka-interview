package org.nebka.tech.interview

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import java.math.BigDecimal
import java.math.BigInteger

typealias Decimal = BigDecimal

fun <T> Tracer.createChildSpan(
        context: Context,
        spanName: String,
        attributes: Map<String, Any>? = null,
        function: (Span, Context) -> T
): T {
    val span = this.spanBuilder(spanName).setParent(context).startSpan()
    if (!attributes.isNullOrEmpty())
        attributes.forEach { (key, value) ->
            span.setCorrectAttribute(key, value)
        }
    return try {
        // Make the new span's context active
        span.makeCurrent().use {
            val spanContext = Context.current()
            function(span, spanContext)
        }
    } catch (e: Exception) {
        span.setCorrectAttribute("exceptionMessage", e.message)
        throw e
    } finally {
        span.end()  // Always end the span
    }
}

fun Span.setCorrectAttributes(attributes: Map<String, Any?>) {
    attributes.forEach { (key, value) -> this.setCorrectAttribute(key, value) }
}

fun Span.setCorrectAttribute(key: String, value: Any?): Span {
    value ?: return this
    try {
        when (value) {
            is Boolean -> this.setAttribute(key, value)
            is Long -> this.setAttribute(key, value)
            is Byte -> this.setAttribute(key, value.toLong())
            is Short -> this.setAttribute(key, value.toLong())
            is Int -> this.setAttribute(key, value.toLong())
            is BigInteger -> this.setAttribute(key, value.toLong())
            is Double -> this.setAttribute(key, value)
            is BigDecimal -> this.setAttribute(key, value.toDouble())
            is Float -> this.setAttribute(key, value.toDouble())
            else -> this.setAttribute(key, value.toString())
        }
    } catch (e: Exception) {
        this.setAttribute(key, value.toString())
    }
    return this
}