package org.ulalax.playhouse.simple.client

import org.apache.logging.log4j.kotlin.logger
import org.ulalax.playhouse.client.ApiPacketListener
import org.ulalax.playhouse.client.StagePacketListener
import org.ulalax.playhouse.client.network.message.Packet
import org.ulalax.playhouse.simple.Simple.*


class SimpleApiPacketListener : ApiPacketListener {
    private val log = logger()
    override fun onReceive(serviceId: Short, packet: Packet) {
        log.info("client onReceive:$serviceId, ${packet.msgId}")

        if(packet.msgId == ChatMsg.getDescriptor().index){
            log.info("chat msg:${ChatMsg.parseFrom(packet.data()).data}")
        }
    }
}

class SimpleStagePacketListener : StagePacketListener {
    private val log = logger()

    override fun onReceive(serviceId: Short, stageIndex:Int,packet: Packet) {
        log.info("stage message onReceive:$serviceId,$stageIndex, ${packet.msgId}")

        if(packet.msgId == ChatMsg.getDescriptor().index){
            log.info("chat msg:${ChatMsg.parseFrom(packet.data()).data}")
        }
    }
}