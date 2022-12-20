package com.lifemmo.pl.simple.room

import com.lifemmo.pl.base.protocol.Packet
import com.lifemmo.pl.base.service.BaseSender
import com.lifemmo.pl.base.service.ServerSystem
import com.lifemmo.pl.base.service.SystemPanel
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