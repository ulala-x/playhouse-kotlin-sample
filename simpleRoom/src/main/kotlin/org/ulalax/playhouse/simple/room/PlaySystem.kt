package org.ulalax.playhouse.simple.room

import org.ulalax.playhouse.protocol.Packet
import org.ulalax.playhouse.service.BaseSender
import org.ulalax.playhouse.service.ServerSystem
import org.ulalax.playhouse.service.SystemPanel
import org.apache.logging.log4j.kotlin.logger

class PlaySystem(override val systemPanel: SystemPanel, override val baseSender: BaseSender) : ServerSystem {
    private val log = logger()
    override suspend fun onDispatch(packet: Packet) {
        log.info("onDispatch : ${packet.msgName}")
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