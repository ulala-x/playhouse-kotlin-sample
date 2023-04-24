package org.ulalax.playhouse.simple.api.handler

import lombok.extern.slf4j.Slf4j
import org.apache.logging.log4j.kotlin.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.ulalax.playhouse.communicator.message.Packet
import org.ulalax.playhouse.communicator.message.ReplyPacket
import org.ulalax.playhouse.service.ApiSender
import org.ulalax.playhouse.service.Sender
import org.ulalax.playhouse.service.SystemPanel
import org.ulalax.playhouse.service.api.ApiService
import org.ulalax.playhouse.service.api.BackendHandlerRegister
import org.ulalax.playhouse.service.api.HandlerRegister
import org.ulalax.playhouse.simple.Simple.*
import org.ulalax.playhouse.simple.api.SpringContext
import java.text.SimpleDateFormat
import java.util.*

@Slf4j
@Component
class SampleApi : ApiService {
    private lateinit var systemPanel: SystemPanel
    private lateinit var sender: Sender
    private val log = logger()


    override suspend fun init(systemPanel: SystemPanel, sender: Sender) {
        this.systemPanel = systemPanel
        this.sender = sender
    }

    override fun instance(): ApiService {
        return SpringContext.getContext().getBean(this::class.java)
    }

    override fun handles(register: HandlerRegister,backendHandlerRegister: BackendHandlerRegister) {
        register.add(AuthenticateReq.getDescriptor().index,::authenticate)
        register.add(HelloReq.getDescriptor().index,::hello)
        register.add(CloseSessionMsg.getDescriptor().index,::closeSessionMsg)
        register.add(SendMsg.getDescriptor().index,::sendMessage)
    }

    fun authenticate(packet: Packet, apiSender: ApiSender) {
        val req: AuthenticateReq = AuthenticateReq.parseFrom(packet.data())
        val accountId: Long = req.userId
        log.info("authenticate: $accountId,${req.token}")
        apiSender.authenticate(accountId)
        val message: AuthenticateRes = AuthenticateRes.newBuilder().setUserInfo(accountId.toString()).build()
        apiSender.reply(ReplyPacket(message))
    }

    fun hello(packet: Packet, apiSender: ApiSender) {
        val req: HelloReq = HelloReq.parseFrom(packet.data())
        log.info("hello:${req.message}, ${apiSender.accountId}" )
        apiSender.reply(ReplyPacket(HelloRes.newBuilder().setMessage("hello").build()))
    }

    fun sendMessage(packet:Packet,apiSender: ApiSender){
        val recv = SendMsg.parseFrom(packet.data())
        log.info("message:${recv.message}")
        apiSender.sendToClient(Packet(SendMsg.getDescriptor().index,packet.movePayload()))
    }

    fun closeSessionMsg( packet: Packet, apiSender: ApiSender) {
        apiSender.sendToClient(Packet(CloseSessionMsg.newBuilder().build()))
        apiSender.sessionClose(apiSender.sessionEndpoint, apiSender.sid)
    }

    @Scheduled(fixedRate = 5000)
    fun reportCurrentTime() {
        log.info("The time is now ${dateFormat.format(Date())}")
    }

    companion object {
        private val dateFormat = SimpleDateFormat("HH:mm:ss")
    }


}