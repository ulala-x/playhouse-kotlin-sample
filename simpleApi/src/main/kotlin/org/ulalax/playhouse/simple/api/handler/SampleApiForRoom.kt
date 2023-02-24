package org.ulalax.playhouse.simple.api.handler

import lombok.extern.slf4j.Slf4j
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Component
import org.ulalax.playhouse.protocol.Packet
import org.ulalax.playhouse.protocol.ReplyPacket
import org.ulalax.playhouse.service.*
import org.ulalax.playhouse.service.ControlContext.baseSender
import org.ulalax.playhouse.service.api.annotation.Api
import org.ulalax.playhouse.service.api.annotation.ApiBackendHandler
import org.ulalax.playhouse.service.api.annotation.ApiHandler
import org.ulalax.playhouse.service.api.annotation.Init
import org.ulalax.playhouse.simple.Simple
import org.ulalax.playhouse.simple.Simple.*

@Slf4j
@Component
@Api
class SampleApiForRoom {
    private val log = logger()

    private lateinit var systemPanel: SystemPanel
    private lateinit var apiBaseSender: ApiBaseSender
    private val roomSvcId = "room"
    private val roomType = "simple"
    private val success = 0
    private val fail = 1

    @Init
    fun init(systemPanel: SystemPanel, apiBaseSender: ApiBaseSender) {
        this.systemPanel = systemPanel
        this.apiBaseSender = apiBaseSender
    }

    @ApiHandler(msgName = "CreateRoomReq")
    fun createStage(sessionInfo: String?, packet: Packet, apiSender: ApiSender) {
        log.info("CreateRoom : sessionInfo:${sessionInfo},msgName:${packet.msgName}")
        val data: String = CreateRoomReq.parseFrom(packet.data()).data
        val randRoomServerInfo = systemPanel.randomServerInfo("room")
        val roomEndpoint = randRoomServerInfo.bindEndpoint()
        val result = apiSender.createStage(roomEndpoint, roomType, Packet(CreateRoomAsk.newBuilder().setData(data).build()))
        val createRoomAnswer = CreateRoomAnswer.parseFrom(result.createStageRes.data())
        val stageId = result.stageId
        if (result.isSuccess()) {
            apiSender.reply(
                ReplyPacket(CreateRoomRes.newBuilder().setData(createRoomAnswer.data)
                        .setRoomId(stageId)
                        .setRoomEndpoint(roomEndpoint)
                        .build()
                )
            )
        } else {
            apiSender.reply(ReplyPacket(result.errorCode))
        }
    }

    @ApiHandler(msgName = "JoinRoomReq")
    fun joinStage(sessionInfo: String, packet: Packet, apiSender: ApiSender) {
        log.info("joinRoom : sessionInfo:${sessionInfo},msgName:${packet.msgName}")
        val request: JoinRoomReq = JoinRoomReq.parseFrom(packet.data())
        val data: String = request.data
        val roomId: Long = request.roomId
        val roomEndpoint: String = request.roomEndpoint
        val accountId = sessionInfo.toLong()
        val sessionEndpoint = apiSender.sessionEndpoint()
        val sid = apiSender.sid()
        val result = apiSender.joinStage(
            roomEndpoint,
            roomId,
            accountId,
            sessionEndpoint,
            sid,
            Packet(JoinRoomAsk.newBuilder().setData(data).build())
        )
        if (result.isSuccess()) {
            val joinRoomAnswer = JoinRoomAnswer.parseFrom(result.joinStageRes.data())
            apiSender.reply(ReplyPacket(JoinRoomRes.newBuilder().setData(joinRoomAnswer.data).build()))
        } else {
            apiSender.reply(ReplyPacket(result.errorCode))
        }
    }

    @ApiHandler(msgName = "CreateJoinRoomReq")
    fun createJoinStage(sessionInfo: String, packet: Packet, apiSender: ApiSender) {
        log.info("CreateJoinRoomReq : sessionInfo:${sessionInfo},msgName:${packet.msgName}")
        val request: CreateJoinRoomReq = CreateJoinRoomReq.parseFrom(packet.data())
        val data = request.data
        val roomId: Long = request.roomId
        val roomEndpoint: String = request.roomEndpoint
        val accountId = sessionInfo.toLong()
        val sessionEndpoint = apiSender.sessionEndpoint()
        val sid = apiSender.sid()
        val createPayload = Packet(CreateRoomAsk.newBuilder().setData(data).build())
        val joinPayload = Packet(JoinRoomAsk.newBuilder().setData(data).build())
        val result = apiSender.createJoinStage(
            roomEndpoint, roomType, roomId, createPayload,
            accountId, sessionEndpoint, sid, joinPayload
        )
        if (result.isSuccess()) {
            val joinRoomAnswer = CreateJoinRoomAnswer.parseFrom(result.joinStageRes.data())
            apiSender.reply(
                ReplyPacket(CreateJoinRoomRes.newBuilder().setData(joinRoomAnswer.data).build())
            )
        } else {
            apiSender.reply(ReplyPacket(result.errorCode))
        }
    }

    @ApiBackendHandler(msgName = "LeaveRoomNotify")
    fun leaveRoomNotify(sessionInfo: String?, packet: Packet, apiBackendSender: ApiBackendSender?) {
        log.info("leaveRoomNotify : sessionInfo:${sessionInfo},msgName:${packet.msgName}")
        val baseSender = baseSender
        val notify = LeaveRoomNotify.parseFrom(packet.data())
        baseSender.sendToClient(notify.sessionEndpoint, notify.sid, Packet(notify))
    }

    @ApiBackendHandler(msgName = "HelloToApiReq")
    fun helloToApiReq(sessionInfo: String?, packet: Packet, apiBackendSender: ApiBackendSender) {
        log.info("helloToApiReq : sessionInfo:${sessionInfo},msgName:${packet.msgName}")
        val data: String = HelloToApiReq.parseFrom(packet.data()).data
        apiBackendSender.reply(ReplyPacket(HelloToApiRes.newBuilder().setData(data).build()))
    }
}