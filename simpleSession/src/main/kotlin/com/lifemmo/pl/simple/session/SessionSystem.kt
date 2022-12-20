package com.lifemmo.pl.simple.session

import com.lifemmo.pl.base.protocol.Packet
import com.lifemmo.pl.base.service.BaseSender
import com.lifemmo.pl.base.service.ServerSystem
import com.lifemmo.pl.base.service.SystemPanel
import org.apache.logging.log4j.kotlin.logger

class SessionSystem(override val baseSender: BaseSender, override val systemPanel: SystemPanel) : ServerSystem {
    val log = logger()
    override fun onDispatch(packet: Packet) {
        log.info("${packet.msgName} is received")
    }

    override fun onPause() {
        log.info("session pause")
    }

    override fun onResume() {
        log.info("session resume")
    }

    override fun onStart() {
        log.info("session start")
    }

    override fun onStop() {
        log.info("session stop")
        log.info("session stop")
    }
}