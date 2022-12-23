package org.ulalax.playhouse.simple.room

import org.ulalax.playhouse.protocol.Packet
import org.ulalax.playhouse.service.BaseSender
import org.ulalax.playhouse.service.ServerSystem
import org.ulalax.playhouse.service.SystemPanel
import org.apache.logging.log4j.kotlin.logger

class RoomSystem(override val systemPanel: SystemPanel,override val baseSender: BaseSender) : ServerSystem {
    private val log = logger()
    override fun onDispatch(packet: Packet) {
        log.info("onDispatch : ${packet.msgName}")
    }

    override fun onPause() {
        log.info("onPause")
    }

    override fun onResume() {
        log.info("onResume")
    }

    override fun onStart() {
        log.info("onStart")
    }

    override fun onStop() {
        log.info("onStop")
    }
}