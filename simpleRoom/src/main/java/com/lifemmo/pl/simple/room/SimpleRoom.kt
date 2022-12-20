package com.lifemmo.pl.simple.room

import com.lifemmo.pl.base.protocol.Packet
import com.lifemmo.pl.base.protocol.ReplyPacket
import com.lifemmo.pl.base.service.RoomSender
import com.lifemmo.pl.base.service.room.Room
import com.lifemmo.pl.base.service.room.base.TimerCallback
import com.lifemmo.pl.base.service.room.contents.PacketHandler
import com.lifemmo.pl.simple.Simple
import com.lifemmo.pl.simple.Simple.HelloToApiReq
import com.lifemmo.pl.simple.room.command.ChatMsgCmd
import com.lifemmo.pl.simple.room.command.LeaveRoomCmd
import org.apache.logging.log4j.kotlin.logger
import java.time.Duration


class SimpleRoom(override val roomSender: RoomSender) : Room<SimpleUser> {
    private val log = logger()
    private val packetHandler = PacketHandler<SimpleRoom,SimpleUser>()
    private val userMap:MutableMap<Long,SimpleUser> = mutableMapOf()
    private var count = 0

    val countTimer: TimerCallback = {
        log.info("count timer:$count")
        count++
    }


    init {
        packetHandler.add(Simple.LeaveRoomReq.getDescriptor().name,LeaveRoomCmd())
        packetHandler.add(Simple.ChatMsg.getDescriptor().name,ChatMsgCmd())

        roomSender.addCountTimer(Duration.ofSeconds(3),3, Duration.ofSeconds(1), countTimer)
        roomSender.addRepeatTimer(Duration.ZERO, Duration.ofMillis(200), suspend{
            log.info("repeat timer")
        })
    }


    override suspend fun onCreate(packet: Packet): ReplyPacket {
        log.info("onCreate:${roomSender.roomType()},${roomSender.roomId()},${packet.msgName}")
        val request = Simple.CreateRoomAsk.parseFrom(packet.buffer())
        return ReplyPacket(Simple.CreateRoomAnswer.newBuilder().setData(request.data).build())
    }

    override suspend fun onDispatch(user: SimpleUser, packet: Packet) {
        log.info("onDispatch:${roomSender.roomType()},${roomSender.roomId()},${packet.msgName}")
        packetHandler.dispatch(this, user ,packet)
    }

    override suspend fun onJoinRoom(user: SimpleUser, packet: Packet): ReplyPacket {

        log.info("onJoinRoom:${roomSender.roomType()},${roomSender.roomId()},${packet.msgName}")
        val request = Simple.JoinRoomAsk.parseFrom(packet.buffer())
        return ReplyPacket(Simple.JoinRoomAnswer.newBuilder().setData(request.data).build())

    }

    override suspend fun onPostCreate() {
        log.info("onPostCreate:${roomSender.roomType()},${roomSender.roomId()}")

    }

    override suspend fun onPostJoinRoom(user: SimpleUser) {
        userMap[user.accountId()] = user
        log.info("onPostJoinRoom:${roomSender.roomType()},${roomSender.roomId()},${user.accountId()}")
        val deferred = user.userSender.deferredToApi(
            user.accountId().toString(),
            Packet(HelloToApiReq.newBuilder().setData("hello").build())
        ).await()

        log.info("deferred:${Simple.HelloToApiRes.parseFrom(deferred.buffer()).data}")


    }

    override suspend fun onDisconnect(user: SimpleUser) {
        log.info("onSessionClose:${roomSender.roomType()},${roomSender.roomId()},${user.accountId()}")

        leaveRoom(user)
    }

    fun leaveRoom(user: SimpleUser) {
        userMap.remove(user.accountId())

        log.info("leave room ${user.accountId()}, size:${userMap.size}")

        if(userMap.isEmpty()){
            log.info("add count timer :${Thread.currentThread().id}")
            roomSender.addCountTimer(Duration.ofSeconds(5),1,Duration.ofSeconds(5), suspend {
                if(userMap.isEmpty()){
                    log.info("close room :${Thread.currentThread().id}")
                    roomSender.closeRoom()
                }
            })
        }
    }

    fun sendAll(packet: Packet) {
        userMap.values.forEach {
            it.userSender.sendToClient(packet)
        }
    }

}