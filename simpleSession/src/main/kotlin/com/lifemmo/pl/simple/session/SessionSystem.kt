package org.ulalax.playhouse.pl.simple.session

import org.ulalax.playhouse.protocol.Packet
import org.ulalax.playhouse.service.BaseSender
import org.ulalax.playhouse.service.ServerSystem
import org.ulalax.playhouse.service.SystemPanel
import org.apache.logging.log4j.kotlin.logger

class SessionSystem(override val baseSender: BaseSender, override val systemPanel: SystemPanel) : ServerSystem {
    val log = logger()
    override suspend fun onDispatch(packet: Packet) {
        log.info("${packet.msgName} is received")
    }

    override suspend fun onPause() {
        log.info("session pause")
    }

    override suspend fun onResume() {
        log.info("session resume")
    }

    override suspend fun onStart() {
        log.info("session start")
    }

    override suspend fun onStop() {
        log.info("session stop")
        log.info("session stop")
    }
}