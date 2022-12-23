package org.ulalax.playhouse.simple.client

import org.apache.logging.log4j.kotlin.logger
import org.ulalax.playhouse.client.ClientPacketListener
import org.ulalax.playhouse.protocol.Packet
import org.ulalax.playhouse.simple.Simple.*


class SimpleClientPacketListener : ClientPacketListener {
    private val log = logger()

    override fun onReceive(serviceId: String, packet: Packet) {
        log.info("client onReceive:$serviceId, ${packet.msgName}")

        if(packet.msgName == ChatMsg.getDescriptor().name){
            log.info("chat msg:${ChatMsg.parseFrom(packet.buffer()).data}")
        }
    }
}