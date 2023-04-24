package org.ulalax.playhouse.simple.api

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.kotlin.logger
import org.ulalax.playhouse.communicator.message.Packet
import org.ulalax.playhouse.communicator.message.ReplyPacket
import org.ulalax.playhouse.service.Sender
import org.ulalax.playhouse.service.ServerSystem
import org.ulalax.playhouse.service.SystemPanel
import org.ulalax.playhouse.simple.Simple.*


class ApiSystem(override val systemPanel: SystemPanel,override val sender: Sender) : ServerSystem {
    private val log = logger()

    override suspend fun onDispatch(packet: Packet) {
        log.info("${packet.msgId} packet received")

        try {
            if (packet.msgId == HelloReq.getDescriptor().index) {
                val data: String = HelloReq.parseFrom(packet.data()).message
                sender.reply(ReplyPacket(HelloRes.newBuilder().setMessage(data).build()))
            }
        } catch (e: Exception) {
            log.error(ExceptionUtils.getMessage(e))
        }
    }

    override suspend fun onPause() {
        log.info("onPause")
    }

    override suspend fun onResume() {
        log.info("onResume")
    }

    override suspend fun onStart() {
        log.info("onStart")
    }

    override suspend fun onStop() {
        log.info("onStop")
    }
}