package io.nais.devrapid

import com.google.cloud.bigquery.*
import org.slf4j.LoggerFactory
import java.util.*


class BigQuery {
    private val table = "from_devrapid"
    private val dataset = "deploys"
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

    fun ping(): Boolean {
        return try {
            val queryConfig = QueryJobConfiguration.newBuilder(
                "SELECT 0"
            )
                .setUseLegacySql(false)
                .build()

            val jobId = JobId.of(UUID.randomUUID().toString())
            var queryJob: Job? = bigquery.service.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build())

            queryJob = queryJob!!.waitFor()

            if (queryJob == null) {
                log.error("Job no longer exists")
                return false
            } else if (queryJob.status.error != null) {
                log.error(queryJob.status.error.toString())
                return false
            }
            true
        } catch (e: BigQueryException) {
            log.error(e.message)
            false
        }
    }

    fun write(deployHistoryRow: Map<String, String>) {
        val request = InsertAllRequest.newBuilder(TableId.of(dataset, table))
            .setIgnoreUnknownValues(true)
            .addRow(deployHistoryRow)
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

    fun createTable() {
        val tableId = TableId.of(dataset, table)
        if (bigquery.service.getTable(tableId) != null) {
            log.info("Table $tableId already exists")
            return
        }
        val schema = Schema.of(
            Field.of("correlationId", StandardSQLTypeName.STRING),
            Field.of("platform", StandardSQLTypeName.STRING),
            Field.of("deploymentSystem", StandardSQLTypeName.STRING),
            Field.newBuilder("team", StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
            Field.of("environment", StandardSQLTypeName.STRING),
            Field.newBuilder("namespace", StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder("cluster", StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder("application", StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
            Field.of("version", StandardSQLTypeName.STRING),
            Field.newBuilder("containerImage", StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
            Field.of("deployTime", StandardSQLTypeName.TIMESTAMP),
            Field.newBuilder("gitCommitSha", StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
        )
        val tableDefinition = StandardTableDefinition.of(schema)
        val tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build()
        bigquery.service.create(tableInfo)
    }
}