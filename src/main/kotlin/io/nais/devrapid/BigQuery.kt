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

    fun write(deployHistoryRow: DeployHistoryRow) {
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


data class DeployHistoryRow(
    val deploySha: String,
    val repo: String,
    val language: String,
    val deployTime: ZonedDateTime,
    val pushTime: ZonedDateTime,
    val firstCommitOnBranch: ZonedDateTime?
) {
    private companion object {
        private val log = LoggerFactory.getLogger(DeployHistoryRow::class.java)
    }

    fun asMap(): Map<String, String> {
        val map = mutableMapOf(
            "deploySha" to deploySha,
            "repo" to repo,
            "language" to language,
            "deployTime" to deployTime.asTimeStamp(),
            "pushTime" to pushTime.asTimeStamp(),
        )

        log.info("FirstCommitOnBranch:$firstCommitOnBranch")

        if (firstCommitOnBranch != null) {
            map["firstCommitOnBranch"] = firstCommitOnBranch.asTimeStamp()
        }

        return map.toMap()
    }

    private fun ZonedDateTime.asTimeStamp() = ISO_LOCAL_DATE_TIME.format(this.withZoneSameInstant(ZoneId.of("UTC")))
}