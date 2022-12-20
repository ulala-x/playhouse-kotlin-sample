package com.lifemmo.pl.simple.room.command

import com.lifemmo.pl.base.protocol.Packet
import com.lifemmo.pl.base.service.room.contents.PacketCmd
import com.lifemmo.pl.simple.Simple
import com.lifemmo.pl.simple.room.SimpleRoom
import com.lifemmo.pl.simple.room.SimpleUser
import kotlinx.coroutines.delay

class ChatMsgCmd : PacketCmd<SimpleRoom,SimpleUser> {
    override suspend fun execute(room: SimpleRoom, user: SimpleUser, packet: Packet) {

        room.sendAll(packet)

        room.roomSender.asyncBlock({
               delay(1000)
            "test hi~"
        },{data ->
            room.sendAll(Packet(Simple.ChatMsg.newBuilder().setData(data).build()))
        })


    }
}