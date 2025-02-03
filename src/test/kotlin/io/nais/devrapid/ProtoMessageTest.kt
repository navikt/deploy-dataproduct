package io.nais.devrapid

import io.nais.devrapid.github.Message
import no.nav.protos.deployment.DeploymentEvent
import org.junit.jupiter.api.Test
import com.google.protobuf.Any
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class ProtoMessageTest{
    @Test
    fun `should parse`(){
        val pushMessageProto = anyPushMessageProto()
        val anyByteArray = Any.parseFrom(pushMessageProto.toByteArray())
        assertTrue(anyByteArray.`is`((Message.Push::class.java)))
        assertFalse(anyByteArray.`is`((DeploymentEvent.Event::class.java)))
        assertThat(anyByteArray.unpack(Message.Push::class.java)).isEqualTo(pushMessageProto.unpack(Message.Push::class.java))

        val deploymentProto = anyDeploymentProto()
        val anyDeploymentByteArray =  Any.parseFrom(deploymentProto.toByteArray())
        assertTrue(anyDeploymentByteArray.`is`((DeploymentEvent.Event::class.java)))
        assertFalse(anyDeploymentByteArray.`is`((Message.Push::class.java)))
        assertThat(anyDeploymentByteArray.unpack(DeploymentEvent.Event::class.java)).isEqualTo(deploymentProto.unpack(DeploymentEvent.Event::class.java))

    }

}

