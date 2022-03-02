package io.nais.devrapid

import com.google.protobuf.Any
import com.google.protobuf.Timestamp
import io.nais.devrapid.github.Message
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Gauge
import no.nav.protos.deployment.DeploymentEvent
import no.nav.protos.deployment.DeploymentEvent.RolloutStatus.complete
import org.slf4j.LoggerFactory
import java.time.*
import java.time.Instant.ofEpochSecond
import java.time.ZonedDateTime.ofInstant

class EventCollector {

    private val messages = mutableMapOf<String, Message.Push>()
    private val LOGGER = LoggerFactory.getLogger("deploy-dataproduct")

    companion object {
        private val leadTimeGauge = Gauge.build()
            .name("deployment_leadtime")
            .labelNames("repo")
            .help("Lead time from Github push to completed deployment")
            .register()

        private val messageSize = Gauge.build()
            .name("message_map_size")
            .help("Size of map that holds messages (deploy and push)")
            .register()

        private val leadTimeFromBranchCreatedGauge = Gauge.build()
            .name("deployment_leadtime_from_branch_created")
            .labelNames("repo")
            .help("Lead time from first commit on feature branch to completed deployment")
            .register()
    }

    internal fun collectOrComputeLeadTime(byteArray: ByteArray): DeployHistoryRow? {
        val any = Any.parseFrom(byteArray)
        when {
            any.`is`(Message.Push::class.java) -> {
                val push = any.unpack(Message.Push::class.java)
                LOGGER.info("Received push message (repo: ${push.repositoryName} sha: ${push.latestCommitSha})")
                messages[push.latestCommitSha] = push
                updateMessageSizeGauge()
                return null
            }
            any.`is`(DeploymentEvent.Event::class.java) -> {
                val deploy = any.unpack(DeploymentEvent.Event::class.java)
                val sha = deploy.gitCommitSha
                val push = messages[sha]
                if (push != null && deploy.rolloutStatus == complete) {
                    LOGGER.info("Received deploy message (app: ${deploy.application} sha: ${sha})")
                    computeLeadTime(push, deploy)
                    messages.remove(sha)
                    updateMessageSizeGauge()

                    return DeployHistoryRow(
                        deploySha = deploy.gitCommitSha,
                        repo = push.repositoryName,
                        language = push.programmingLanguage,
                        deployTime = deploy.timestamp.zonedTimestamp(),
                        pushTime = push.webHookRecieved.zonedTimestamp(),
                        firstCommitOnBranch = if (push.hasFirstBranchCommit()) push.firstBranchCommit.zonedTimestamp() else null
                    )
                }
            }
        }
        return null
    }

    private fun computeLeadTime(push: Message.Push, deploy: DeploymentEvent.Event) {
        val leadTime = deploy.timestamp.seconds - push.webHookRecieved.seconds
        if (push.hasFirstBranchCommit()) {
            val leadTimeFromBranchCreated = deploy.timestamp.seconds - push.firstBranchCommit.seconds
            leadTimeFromBranchCreatedGauge.labels(push.repositoryName).set(leadTimeFromBranchCreated.toDouble())
        }
        LOGGER.info("Calculated lead time for deploy with sha ${push.latestCommitSha} in repo ${push.repositoryName} is $leadTime seconds")
        leadTimeGauge.labels(push.repositoryName).set(leadTime.toDouble())
    }

    private fun updateMessageSizeGauge() = messageSize.set(messages.keys.size.toDouble())
    internal fun messageSize() = messages.size


}

private fun Timestamp.zonedTimestamp(): ZonedDateTime {
    return ofInstant(
        ofEpochSecond(this.seconds), ZoneId.systemDefault()
    )
}
