package com.lifemmo.pl.simple.client

import com.lifemmo.client.ClientPacketListener
import com.lifemmo.pl.base.protocol.ClientPacket
import com.lifemmo.pl.base.protocol.Packet
import com.lifemmo.pl.simple.Simple
import com.lifemmo.pl.simple.Simple.ChatMsg
import org.apache.logging.log4j.kotlin.logger


class SimpleClientPacketListener : ClientPacketListener {
    val log = logger()

    override fun onReceive(serviceId: String, packet: Packet) {
        log.info("client onReceive:$serviceId, ${packet.msgName}")

        if(packet.msgName == ChatMsg.getDescriptor().name){
            log.info("chat msg:${ChatMsg.parseFrom(packet.buffer()).data}")
        }
    }
}