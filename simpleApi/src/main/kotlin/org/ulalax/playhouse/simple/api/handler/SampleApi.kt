package org.ulalax.playhouse.simple.api.handler

import lombok.extern.slf4j.Slf4j
import org.apache.logging.log4j.kotlin.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.ulalax.playhouse.protocol.Packet
import org.ulalax.playhouse.protocol.ReplyPacket
import org.ulalax.playhouse.service.ApiBaseSender
import org.ulalax.playhouse.service.ApiSender
import org.ulalax.playhouse.service.SystemPanel
import org.ulalax.playhouse.service.api.annotation.Api
import org.ulalax.playhouse.service.api.annotation.ApiHandler
import org.ulalax.playhouse.service.api.annotation.Init
import org.ulalax.playhouse.simple.Simple.*
import java.text.SimpleDateFormat
import java.util.*

@Slf4j
@Component
@Api
class SampleApi {
    private var systemPanel: SystemPanel? = null
    private var apiBaseSender: ApiBaseSender? = null
    private val log = logger()

    @Init
    fun init(systemPanel: SystemPanel?, apiBaseSender: ApiBaseSender?) {
        this.systemPanel = systemPanel
        this.apiBaseSender = apiBaseSender
    }

    @ApiHandler(msgName = "AuthenticateReq")
    fun authenticate(sessionInfo: String, packet: Packet, apiSender: ApiSender) {
        val req: AuthenticateReq = AuthenticateReq.parseFrom(packet.buffer())
        val accountId: Long = req.userId
        log.info("authenticate: $accountId,${req.token}")
        apiSender.authenticate(accountId, accountId.toString())
        val message: AuthenticateRes = AuthenticateRes.newBuilder().setUserInfo(accountId.toString()).build()
        apiSender.reply(ReplyPacket(message))
    }

    @ApiHandler(msgName = "HelloReq")
    fun hello(sessionInfo: String, packet: Packet, apiSender: ApiSender) {
        val req: HelloReq = HelloReq.parseFrom(packet.buffer())
        log.info("hello:${req.message}, ${apiSender.sessionInfo()}" )
        apiSender.reply(ReplyPacket(HelloRes.newBuilder().setMessage("hello").build()))
    }

    @ApiHandler(msgName = "SessionUpdateMsg")
    fun SessionUpdateMsg(sessionInfo: String, packet: Packet, apiSender: ApiSender) {
        val msg: SessionUpdateMsg = SessionUpdateMsg.parseFrom(packet.buffer())
        log.info("SessionUpdateMsg:${msg.sessionInfo}")
        apiSender.updateSession(apiSender.serviceId(), msg.sessionInfo)
    }

    @ApiHandler(msgName = "SessionInfoReq")
    fun SessionInfoReq(sessionInfo: String, packet: Packet, apiSender: ApiSender) {
        log.info("SessionInfoReq:${sessionInfo}")
        apiSender.reply(ReplyPacket(SessionInfoRes.newBuilder().setSessionInfo(sessionInfo).build()))
    }

    @ApiHandler(msgName = "CloseSessionMsg")
    fun CloseSessionMsg(sessionInfo: String, packet: Packet, apiSender: ApiSender) {
        apiSender.sendToClient(Packet(CloseSessionMsg.newBuilder().build()))
        apiSender.sessionClose(apiSender.sessionEndpoint(), apiSender.sid())
    }

    @Scheduled(fixedRate = 5000)
    fun reportCurrentTime() {
        log.info("The time is now ${dateFormat.format(Date())}")
    }

    companion object {
        private val dateFormat = SimpleDateFormat("HH:mm:ss")
    }
}