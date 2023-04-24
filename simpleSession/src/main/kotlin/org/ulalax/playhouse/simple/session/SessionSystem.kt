package org.ulalax.playhouse.simple.session

import org.ulalax.playhouse.service.ServerSystem
import org.ulalax.playhouse.service.SystemPanel
import org.apache.logging.log4j.kotlin.logger
import org.ulalax.playhouse.communicator.message.Packet
import org.ulalax.playhouse.service.Sender

class SessionSystem(override val systemPanel: SystemPanel,override val sender: Sender) : ServerSystem {

    private val log = logger()
    override suspend fun onDispatch(packet: Packet) {
        log.info("${packet.msgId} is received")
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