package org.ulalax.playhouse.simple.client

import org.ulalax.playhouse.client.Connector
import org.ulalax.playhouse.protocol.Packet
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.ulalax.playhouse.simple.Simple.*
import java.lang.Thread.sleep
import kotlin.system.exitProcess

@SpringBootApplication
class ClientApplication : CommandLineRunner {
    private val log = logger()
    override fun run(vararg args: String?) = runBlocking {
        try{
            val connector = Connector(0,true, SimpleClientPacketListener())
            connector.connect("127.0.0.1",30114)

            val apiSvcId="api"
            val roomSvcId = "room"

            ///////// for api ///////////////////////
            var authenticateRes = connector.request(apiSvcId,
                Packet(AuthenticateReq.newBuilder().setUserId(700).setToken("password").build())
            ).let { AuthenticateRes.parseFrom(it.data()) }

            log.info(authenticateRes.userInfo)

            val helloRes = connector.request(apiSvcId,
                Packet(HelloReq.newBuilder().setMessage("hi!").build())
            ).let { HelloRes.parseFrom(it.data()) }

            log.info(helloRes.message)

            connector.send(apiSvcId,
                Packet(SessionUpdateMsg.newBuilder().setSessionInfo("newSessionInfo").build())
            )

            val newSessionInfo = connector.request(apiSvcId,
                Packet(SessionInfoReq.newBuilder().build())
            ).let { SessionInfoRes.parseFrom(it.data()).sessionInfo }

            log.info("sessionInfo:$newSessionInfo")

            connector.send(apiSvcId,Packet(CloseSessionMsg.newBuilder().build()))

            sleep(1000)

            connector.connect("127.0.0.1",30114)

            ///////// for api ///////////////////////
            authenticateRes = connector.request(apiSvcId,
                Packet(AuthenticateReq.newBuilder().setUserId(700).setToken("password").build())
            ).let { AuthenticateRes.parseFrom(it.data()) }

            log.info(authenticateRes.userInfo)

            //////////for  room /////////////////////
            var roomId: Long
            var roomEndpoint: String
            connector.request(apiSvcId,Packet(CreateRoomReq.newBuilder().setData("success 1").build())
            ).let {
                var res = CreateRoomRes.parseFrom(it.data())
                roomId = res.roomId
                roomEndpoint = res.roomEndpoint
                log.info("${it.msgName} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.request(apiSvcId,Packet(
                JoinRoomReq.newBuilder()
                    .setRoomEndpoint(roomEndpoint)
                    .setRoomId(roomId).setData("success 2")
                .build())
            ).let {
                var res = JoinRoomRes.parseFrom(it.data())
                log.info("${it.msgName} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.request(roomSvcId,Packet(LeaveRoomReq.newBuilder().setData("success 3").build())
            ).let {
                var res = LeaveRoomRes.parseFrom(it.data())
                log.info("${it.msgName} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.request(apiSvcId,Packet(
                CreateJoinRoomReq.newBuilder()
                    .setRoomEndpoint(roomEndpoint)
                    .setRoomId(roomId)
                    .setData("success 4")
                .build())
            ).let {
                var res = CreateJoinRoomRes.parseFrom(it.data())
                log.info("${it.msgName} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.send(roomSvcId,Packet(ChatMsg.newBuilder().setData("hi!").build()))





            delay(3000)

            exitProcess(0)
        }catch (e:Exception){
            log.error(ExceptionUtils.getStackTrace(e))
        }

    }

}

fun main(args:Array<String>) {
    runApplication<ClientApplication>(*args)

}