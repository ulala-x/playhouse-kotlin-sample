package org.ulalax.playhouse.simple.room.command

import org.ulalax.playhouse.communicator.message.Packet
import org.ulalax.playhouse.communicator.message.ReplyPacket
import org.ulalax.playhouse.service.play.contents.PacketCmd
import org.ulalax.playhouse.simple.Simple.*
import org.ulalax.playhouse.simple.room.SimpleRoom
import org.ulalax.playhouse.simple.room.SimpleUser

class LeaveRoomCmd : PacketCmd<SimpleRoom, SimpleUser> {
    override suspend fun execute(room: SimpleRoom, user: SimpleUser, packet: Packet) {

        val request = LeaveRoomReq.parseFrom(packet.data())

        user.actorSender.sendToApi(
            Packet(
                LeaveRoomNotify.newBuilder()
                    .setSessionEndpoint(user.actorSender.sessionEndpoint())
                    .setSid(user.actorSender.sid())
                    .setData(request.data).build()
            )
        )

        room.leaveStage(user)
        user.actorSender.leaveStage()

        room.stageSender.reply(ReplyPacket(LeaveRoomRes.newBuilder().setData(request.data).build()))
    }
}