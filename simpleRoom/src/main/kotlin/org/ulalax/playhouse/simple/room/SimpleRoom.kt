package org.ulalax.playhouse.simple.room

import org.ulalax.playhouse.service.StageSender
import org.ulalax.playhouse.simple.room.command.LeaveRoomCmd
import org.apache.logging.log4j.kotlin.logger
import org.ulalax.playhouse.communicator.message.Packet
import org.ulalax.playhouse.communicator.message.ReplyPacket
import org.ulalax.playhouse.service.TimerCallback
import org.ulalax.playhouse.service.play.Stage
import org.ulalax.playhouse.service.play.contents.PacketHandler
import org.ulalax.playhouse.simple.Simple.*
import org.ulalax.playhouse.simple.room.command.ChatMsgCmd
import java.time.Duration


class SimpleRoom(override val stageSender: StageSender) : Stage<SimpleUser> {
    private val log = logger()
    private val packetHandler = PacketHandler<SimpleRoom, SimpleUser>()
    private val userMap:MutableMap<Long, SimpleUser> = mutableMapOf()
    private var count = 0

    val countTimer: TimerCallback = {
        log.info("count timer:$count")
        count++
    }
    init {
        packetHandler.add(LeaveRoomReq.getDescriptor().index, LeaveRoomCmd())
        packetHandler.add(ChatMsg.getDescriptor().index, ChatMsgCmd())

        stageSender.addCountTimer(Duration.ofSeconds(3),3, Duration.ofSeconds(1), countTimer)
        stageSender.addRepeatTimer(Duration.ZERO, Duration.ofMillis(200), suspend{
            log.info("repeat timer")
        })
    }


    override suspend fun onCreate(packet: Packet): ReplyPacket {
        log.info("onCreate:${stageSender.stageType},${stageSender.stageId},${packet.msgId}")
        val request = CreateRoomAsk.parseFrom(packet.data())
        return ReplyPacket(CreateRoomAnswer.newBuilder().setData(request.data).build())
    }

    override suspend fun onDispatch(user: SimpleUser, packet: Packet) {
        log.info("onDispatch:${stageSender.stageType},${stageSender.stageId},${packet.msgId}")
        packetHandler.dispatch(this, user ,packet)
    }

    override suspend fun onJoinStage(user: SimpleUser, packet: Packet): ReplyPacket {

        log.info("onJoinStage:${stageSender.stageType},${stageSender.stageId},${packet.msgId}")
        val request = JoinRoomAsk.parseFrom(packet.data())
        return ReplyPacket(JoinRoomAnswer.newBuilder().setData(request.data).build())

    }

    override suspend fun onPostCreate() {
        log.info("onPostCreate:${stageSender.stageType},${stageSender.stageId}")

    }

    override suspend fun onPostJoinStage(user: SimpleUser) {
        userMap[user.accountId()] = user
        log.info("onPostJoinStage:${stageSender.stageType},${stageSender.stageId},${user.accountId()}")
        val deferred = user.actorSender.asyncToApi(
            Packet(HelloToApiReq.newBuilder().setData("hello").build())
        ).await()

        log.info("deferred:${HelloToApiRes.parseFrom(deferred.data()).data}")


    }

    override suspend fun onDisconnect(user: SimpleUser) {
        log.info("onDisconnect:${stageSender.stageType},${stageSender.stageId},${user.accountId()}")

        leaveRoom(user)
    }

    fun leaveRoom(user: SimpleUser) {
        userMap.remove(user.accountId())

        log.info("leave room ${user.accountId()}, size:${userMap.size}")

        if(userMap.isEmpty()){
            log.info("add count timer :${Thread.currentThread().id}")
            stageSender.addCountTimer(Duration.ofSeconds(5),1,Duration.ofSeconds(5), suspend {
                if(userMap.isEmpty()){
                    log.info("close room :${Thread.currentThread().id}")
                    stageSender.closeStage() //.closeStage()
                }
            })
        }
    }

    fun sendAll(packet: Packet) {
        userMap.values.forEach {
            it.actorSender.sendToClient(packet)
        }
    }

}