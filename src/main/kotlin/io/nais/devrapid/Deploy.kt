package io.nais.devrapid

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

private val LOGGER = LoggerFactory.getLogger("deploy-dataproduct")


fun Application.deploy() {
    install(CallLogging) {
        level = Level.INFO
        filter { call ->
            !call.request.path().startsWith("/internal")
        }
    }
    install(DefaultHeaders)
    install(ContentNegotiation) {
        json()
    }
    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(
            PrometheusConfig.DEFAULT,
            PrometheusRegistry.defaultRegistry,
            Clock.SYSTEM
        )
        meterBinders = listOf(
            ClassLoaderMetrics(),
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics()
        )
    }
    val bq = BigQuery()
    routing {
        nais(bq)
    }
    val configuration = Configuration()
    bq.createTable()
    launch {
        DeployKafkaConsumer(configuration, bq).run()
    }
}
