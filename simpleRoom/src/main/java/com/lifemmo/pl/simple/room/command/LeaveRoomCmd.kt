package com.lifemmo.pl.simple.room.command

import com.lifemmo.pl.base.protocol.Packet
import com.lifemmo.pl.base.protocol.ReplyPacket
import com.lifemmo.pl.base.service.room.contents.PacketCmd
import com.lifemmo.pl.simple.Simple
import com.lifemmo.pl.simple.Simple.LeaveRoomNotify
import com.lifemmo.pl.simple.room.SimpleRoom
import com.lifemmo.pl.simple.room.SimpleUser

class LeaveRoomCmd : PacketCmd<SimpleRoom,SimpleUser> {
    override suspend fun execute(room: SimpleRoom, user: SimpleUser, packet: Packet) {

        val request = Simple.LeaveRoomReq.parseFrom(packet.buffer())

        user.userSender.sendToApi(
            user.accountId().toString(),
            Packet(
                LeaveRoomNotify.newBuilder()
                    .setSessionEndpoint(user.userSender.sessionEndpoint())
                    .setSid(user.userSender.sid())
                    .setData(request.data).build()
            )
        )

        room.leaveRoom(user)
        user.userSender.leaveRoom()

        room.roomSender.reply(ReplyPacket(Simple.LeaveRoomRes.newBuilder().setData(request.data).build()))
    }
}