package org.ulalax.playhouse.simple.api

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.kotlin.logger
import org.ulalax.playhouse.protocol.Packet
import org.ulalax.playhouse.protocol.ReplyPacket
import org.ulalax.playhouse.service.BaseSender
import org.ulalax.playhouse.service.ServerSystem
import org.ulalax.playhouse.service.SystemPanel
import org.ulalax.playhouse.simple.Simple.*


class ApiSystem(override val systemPanel: SystemPanel,override val baseSender: BaseSender) : ServerSystem {
    private val log = logger()

    override suspend fun onDispatch(packet: Packet) {
        log.info("${packet.msgName} packet received")

        try {
            if (packet.msgName == HelloReq.getDescriptor().name) {
                val data: String = HelloReq.parseFrom(packet.data()).message
                baseSender.reply(ReplyPacket(HelloRes.newBuilder().setMessage(data).build()))
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