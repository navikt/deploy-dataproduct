package io.nais.devrapid

import no.nav.protos.deployment.DeploymentEvent.RolloutStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EventCollectorTest {


    @Test
    fun `should correlate push and deploy events`() {
        val collector = EventCollector()
        collector.extractDeployData(anyPushMessageProto(sha = "1").toByteArray())
        collector.extractDeployData(anyPushMessageProto(sha = "2").toByteArray())
        assertThat(collector.messageSize()).isEqualTo(2)
        collector.extractDeployData(anyDeploymentProto(sha = "1", rolloutStatus = RolloutStatus.complete).toByteArray())
        assertThat(collector.messageSize()).isEqualTo(1)
    }

    @Test
    fun `when deploy event is collected without correponding push, the event is dropped`() {
        val collector = EventCollector()
        collector.extractDeployData(anyDeploymentProto(sha = "1", rolloutStatus = RolloutStatus.complete).toByteArray())
        assertThat(collector.messageSize()).isEqualTo(0)
    }
}