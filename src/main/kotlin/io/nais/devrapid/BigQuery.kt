package io.nais.devrapid

import com.google.cloud.bigquery.BigQueryException
import com.google.cloud.bigquery.BigQueryOptions
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.TableId
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME


class BigQuery {
    private val table = "deploy_history"
    private val dataset = "devrapid_leadtime"
    private val project = "nais-analyse-prod-2dcc"

    private companion object {
        private val log = LoggerFactory.getLogger(BigQuery::class.java)
    }

    private val errorCounter = BiqQueryErrorCounter()

    private val bigquery =
        BigQueryOptions.newBuilder()
            .setLocation("europe-north1")
            .setProjectId(project)
            .build()

    fun write(deployHistoryRow: DeployRow) {
        val request = InsertAllRequest.newBuilder(TableId.of(dataset, table))
            .setIgnoreUnknownValues(true)
            .addRow(deployHistoryRow.asMap())
            .build()

        try {
            val response = bigquery.service.insertAll(request)
            if (response.hasErrors()) response.insertErrors.entries.forEach { log.error("insertError: ${it.value}") }
        } catch (e: BigQueryException) {
            errorCounter.countError()
            log.error(e.message)
            throw e
        }
    }

}


data class DeployRow(
    val correlationId: String,
    val platform: String,
    val deploymentSystem: String,
    val team: String,
    val environment: String,
    val namespace: String,
    val cluster: String,
    val application: String,
    val version: String,
    val containerImage: String,
    val deployTime: ZonedDateTime,
    val gitCommitSha: String
) {
    private companion object {
        private val log = LoggerFactory.getLogger(DeployRow::class.java)
    }

    fun asMap(): Map<String, String> {
        val map = mutableMapOf(
            "correlationId" to correlationId,
            "platform" to platform,
            "deploymentSystem" to deploymentSystem,
            "team" to team,
            "environment" to environment,
            "namespace" to namespace,
            "cluster" to cluster,
            "application" to application,
            "version" to version,
            "containerImage" to containerImage,
            "deployTime" to deployTime.asTimeStamp(),
            "gitCommitSha" to gitCommitSha,
        )

        return map.toMap()
    }

    private fun ZonedDateTime.asTimeStamp() = ISO_LOCAL_DATE_TIME.format(this.withZoneSameInstant(ZoneId.of("UTC")))
}