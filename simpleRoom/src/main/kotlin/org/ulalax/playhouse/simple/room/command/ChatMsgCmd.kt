package org.ulalax.playhouse.simple.room.command

import org.ulalax.playhouse.simple.room.SimpleRoom
import org.ulalax.playhouse.simple.room.SimpleUser
import kotlinx.coroutines.delay
import org.ulalax.playhouse.communicator.message.Packet
import org.ulalax.playhouse.service.play.contents.PacketCmd
import org.ulalax.playhouse.simple.Simple.*

class ChatMsgCmd : PacketCmd<SimpleRoom, SimpleUser> {
    override suspend fun execute(room: SimpleRoom, user: SimpleUser, packet: Packet) {

        room.sendAll(packet)

        room.stageSender.asyncBlock({
               delay(1000)
            "test hi~"
        },{data ->
            room.sendAll(Packet(ChatMsg.newBuilder().setData(data).build()))
        })


    }
}