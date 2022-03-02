package io.nais.devrapid

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class BigQueryKtTest {
    val date = ZonedDateTime.parse("2021-05-19T09:09:01+02:00[Europe/Oslo]")


    @Test
    fun `date string is without zone`() {

        val row = DeployHistoryRow(
            deploySha = "123",
            repo = "repo",
            language = "language",
            deployTime = date,
            pushTime = date,
            firstCommitOnBranch = date
        )
        assertThat(row.asMap()["deployTime"]).isEqualTo("2021-05-19T07:09:01")
    }

    @Test
    fun `handles null for firstcommit`() {

        val row = DeployHistoryRow(
            deploySha = "123",
            repo = "repo",
            language = "language",
            deployTime = ZonedDateTime.now(),
            pushTime = date,
            firstCommitOnBranch = null
        )
        assertNull(row.asMap()["firstCommitOnBranch"])

    }
}