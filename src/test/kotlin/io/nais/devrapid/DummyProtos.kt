package io.nais.devrapid

import com.google.protobuf.AbstractMessage
import com.google.protobuf.Any
import com.google.protobuf.Timestamp
import io.nais.devrapid.github.Message
import no.nav.protos.deployment.DeploymentEvent
import java.time.ZoneId
import java.time.ZonedDateTime

private val commonSha = "123456789123456789"
val jan1_2020 = ZonedDateTime.of(2020, 1, 1, 1, 1, 0, 0, ZoneId.of("Europe/Oslo"))


internal fun anyDeploymentProto(
    sha:String = commonSha,
    rolloutStatus: DeploymentEvent.RolloutStatus = DeploymentEvent.RolloutStatus.complete,
    deployDateTime: ZonedDateTime = jan1_2020
): Any {
    return DeploymentEvent.Event.newBuilder()
        .setApplication("application")
        .setTimestamp(Timestamp.newBuilder().setSeconds(deployDateTime.toEpochSecond()))
        .setRolloutStatus(rolloutStatus)
        .setGitCommitSha(sha)
        .build()
        .asAny()
}

internal fun anyPushMessageProto(sha:String = commonSha, pushDateTime: ZonedDateTime = jan1_2020): Any {
    val timestamp = Timestamp.newBuilder().setSeconds(pushDateTime.toEpochSecond())
    return Message.Push.newBuilder()
        .setLatestCommitSha(sha)
        .setLatestCommit(timestamp)
        .setWebHookRecieved(timestamp)
        .setRef("ref")
        .setMasterBranch("masterBranch")
        .setProgrammingLanguage("programmingLanguage")
        .setRepositoryName("repositoryName")
        .setPrivateRepo(false)
        .setOrganizationName("organizationName")
        .setFilesAdded(1)
        .setFilesDeleted(2)
        .setFilesModified(3)
        .addAllCommitMessages(listOf("commitMessages"))
        .setCoAuthors(1)
        .build()
        .asAny()
}

private fun AbstractMessage.asAny() = Any.pack(this)
