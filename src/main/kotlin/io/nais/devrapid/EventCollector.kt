package io.nais.devrapid

import com.google.protobuf.Any
import com.google.protobuf.Timestamp
import io.nais.devrapid.github.Message
import io.prometheus.client.Gauge
import no.nav.protos.deployment.DeploymentEvent
import no.nav.protos.deployment.DeploymentEvent.RolloutStatus.complete
import org.slf4j.LoggerFactory
import java.time.*
import java.time.Instant.ofEpochSecond
import java.time.ZonedDateTime.ofInstant
import java.time.format.DateTimeFormatter

class EventCollector {

    private val LOGGER = LoggerFactory.getLogger("deploy-dataproduct")

    internal fun extractDeployData(byteArray: ByteArray): Map<String, String>? {
        val any = Any.parseFrom(byteArray)
        when {
            any.`is`(DeploymentEvent.Event::class.java) -> {
                val deploy = any.unpack(DeploymentEvent.Event::class.java)
                if (deploy.rolloutStatus == complete) {
                    LOGGER.info("Received deploy message (app: ${deploy.application} sha: ${deploy.gitCommitSha})")

                    return mutableMapOf(
                        "correlationId" to deploy.correlationID,
                        "platform" to deploy.platform.type.name,
                        "deploymentSystem" to deploy.source.name,
                        "team" to deploy.team,
                        "environment" to deploy.environment.name,
                        "namespace" to deploy.namespace,
                        "cluster" to deploy.cluster,
                        "application" to deploy.application,
                        "version" to deploy.version,
                        "containerImage" to deploy.image.asString(),
                        "deployTime" to deploy.timestamp.utcTimestamp(),
                        "gitCommitSha" to deploy.gitCommitSha,
                    ).toMap()
                }
            }
        }
        return null
    }

}

private fun Timestamp.utcTimestamp() =
    DateTimeFormatter.ISO_LOCAL_DATE_TIME
        .format(ofInstant(ofEpochSecond(this.seconds), ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")))

private fun DeploymentEvent.ContainerImage.asString() =
    this.name + this.tag + this.hash