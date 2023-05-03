package org.ulalax.playhouse.simple.api.handler

import com.google.protobuf.Descriptors.Descriptor
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Component
import org.ulalax.playhouse.communicator.message.Packet
import org.ulalax.playhouse.communicator.message.ReplyPacket
import org.ulalax.playhouse.service.*
import org.ulalax.playhouse.service.ControlContext.baseSender
import org.ulalax.playhouse.service.api.ApiService
import org.ulalax.playhouse.service.api.BackendHandlerRegister
import org.ulalax.playhouse.service.api.HandlerRegister
import org.ulalax.playhouse.simple.Simple.*
import org.ulalax.playhouse.simple.api.SpringContext

@Component
class SampleApiForRoom : ApiService {
    private val log = logger()

    private lateinit var systemPanel: SystemPanel
    private lateinit var sender: Sender
    private val roomSvcId = "room"
    private val roomType = "simple"
    private val success = 0
    private val fail = 1
    private val roomServiceId:Short = 3

    override suspend fun init(systemPanel: SystemPanel, sender: Sender) {
        this.systemPanel = systemPanel
        this.sender = sender
    }
    override fun handles(register: HandlerRegister,backendRegister: BackendHandlerRegister) {
        register.add(CreateRoomReq.getDescriptor().index,::createStage)
        register.add(JoinRoomReq.getDescriptor().index,::joinStage)
        register.add(CreateJoinRoomReq.getDescriptor().index,::createJoinStage)

        backendRegister.add(LeaveRoomNotify.getDescriptor().index,::leaveRoomNotify)
        backendRegister.add(HelloReq.getDescriptor().index,::helloToApiReq)
    }

    override fun instance(): ApiService {
        return SpringContext.getContext().getBean(this::class.java)
    }

    suspend fun createStage(packet: Packet, apiSender: ApiSender) {
        log.info("CreateRoom : accountId:${apiSender.accountId}," +
                "msgName:${ getDescriptor().messageTypes.find { it.index == packet.msgId }!!.name}")

        val data: String = CreateRoomReq.parseFrom(packet.data()).data
        val randRoomServerInfo = systemPanel.getServerInfoByService(roomServiceId)
        val roomEndpoint = randRoomServerInfo.bindEndpoint()
        val stageId = this.systemPanel.generateUUID()
        val result = apiSender.createStage(roomEndpoint, roomType,stageId, Packet(CreateRoomAsk.newBuilder().setData(data).build()))

        val createRoomAnswer = CreateRoomAnswer.parseFrom(result.createStageRes.data())

        log.info("stageId:$stageId")

        if (result.isSuccess()) {
            apiSender.reply(
                ReplyPacket(CreateRoomRes.newBuilder().setData(createRoomAnswer.data)
                        .setStageId(stageId)
                        .setPlayEndpoint(roomEndpoint)
                        .build()
                )
            )
        } else {
            apiSender.reply(ReplyPacket(result.errorCode))
        }
    }

    suspend fun joinStage( packet: Packet, apiSender: ApiSender) {
        log.info("joinRoom : accountId:${apiSender.accountId}, sid:${apiSender.sid}," +
                "msgName:${getDescriptor().messageTypes.find { it.index == packet.msgId }!!.name}")

        val request: JoinRoomReq = JoinRoomReq.parseFrom(packet.data())
        val data: String = request.data
        val stageId: Long = request.roomId
        val roomEndpoint: String = request.playEndpoint
        val result = apiSender.joinStage(
            roomEndpoint,
            stageId,
            Packet(JoinRoomAsk.newBuilder().setData(data).build())
        )
        if (result.isSuccess()) {
            log.info("stageIdx:${result.stageIndex}")

            val joinRoomAnswer = JoinRoomAnswer.parseFrom(result.joinStageRes.data())
            apiSender.reply(ReplyPacket(JoinRoomRes.newBuilder().setData(joinRoomAnswer.data).setStageIdx(result.stageIndex).build()))
        } else {
            apiSender.reply(ReplyPacket(result.errorCode))
        }
    }

    suspend fun createJoinStage(packet: Packet, apiSender: ApiSender) {
        log.info("CreateJoinRoomReq : accountId:${apiSender.accountId},sid:${apiSender.sid}" +
                "msgName:${getDescriptor().messageTypes.find { it.index == packet.msgId }!!.name}")

        val request: CreateJoinRoomReq = CreateJoinRoomReq.parseFrom(packet.data())
        val data = request.data
        val stageId: Long = request.roomId
        val roomEndpoint: String = request.playEndpoint
        val createPayload = Packet(CreateRoomAsk.newBuilder().setData(data).build())
        val joinPayload = Packet(JoinRoomAsk.newBuilder().setData(data).build())
        val result = apiSender.createJoinStage(
            roomEndpoint,
            roomType,
            stageId,
            createPayload,
            joinPayload
        )
        if (result.isSuccess()) {
            val joinRoomAnswer = CreateJoinRoomAnswer.parseFrom(result.joinStageRes.data())
            apiSender.reply(
                ReplyPacket(CreateJoinRoomRes.newBuilder().setData(joinRoomAnswer.data).setStageIdx(result.stageIndex).build())
            )
        } else {
            apiSender.reply(ReplyPacket(result.errorCode))
        }
    }

    suspend fun leaveRoomNotify(packet: Packet, apiBackendSender: ApiBackendSender) {
        log.info("leaveRoomNotify : accountId:${apiBackendSender.accountId}" +
                "msgName:${getDescriptor().messageTypes.find { it.index == packet.msgId }!!.name}")

        val baseSender = baseSender
        val notify = LeaveRoomNotify.parseFrom(packet.data())
        baseSender.sendToClient(notify.sessionEndpoint, notify.sid, Packet(notify))
    }

    suspend fun helloToApiReq(packet: Packet, apiBackendSender: ApiBackendSender) {
        log.info("helloToApiReq : accountId:${apiBackendSender.accountId}" +
                "msgName:${getDescriptor().messageTypes.find { it.index == packet.msgId }!!.name}")

        val data: String = HelloToApiReq.parseFrom(packet.data()).data
        apiBackendSender.reply(ReplyPacket(HelloToApiRes.newBuilder().setData(data).build()))
    }


}