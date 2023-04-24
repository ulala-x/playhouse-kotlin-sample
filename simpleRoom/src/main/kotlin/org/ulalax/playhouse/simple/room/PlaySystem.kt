package org.ulalax.playhouse.simple.room

import org.ulalax.playhouse.service.ServerSystem
import org.ulalax.playhouse.service.SystemPanel
import org.apache.logging.log4j.kotlin.logger
import org.ulalax.playhouse.communicator.message.Packet
import org.ulalax.playhouse.service.Sender

class PlaySystem(override val systemPanel: SystemPanel, override val sender: Sender) : ServerSystem {
    private val log = logger()
    override suspend fun onDispatch(packet: Packet) {
        log.info("onDispatch : ${packet.msgId}")
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