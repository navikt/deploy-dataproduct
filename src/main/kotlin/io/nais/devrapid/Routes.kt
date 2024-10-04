package io.nais.devrapid


import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.common.TextFormat
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.slf4j.LoggerFactory
import kotlin.text.Charsets.UTF_8

private val LOGGER = LoggerFactory.getLogger("deploy-dataproduct")

fun Route.nais(bigQuery: BigQuery) {
    get("/internal/isalive") {
        if (bigQuery.ping()) {
            call.respondText("UP")
        } else {
            call.respondText("BigQuery", status = HttpStatusCode.ServiceUnavailable)
        }

    }
    get("/internal/isready") {
        call.respondText("UP")
    }
    get("/internal/prometheus") {
        val names = call.request.queryParameters.getAll("name")?.toSet() ?: emptySet()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004), HttpStatusCode.OK) {
            TextFormat.write004(this, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names))
        }
    }
}
