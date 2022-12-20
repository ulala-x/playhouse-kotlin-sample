package com.lifemmo.pl.simple.session

import com.lifemmo.client.Connector
import com.lifemmo.pl.base.protocol.Packet
import com.lifemmo.pl.simple.Simple
import com.lifemmo.pl.simple.Simple.*
import com.lifemmo.pl.simple.client.SimpleClientPacketListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.lang.Thread.sleep
import kotlin.system.exitProcess

@SpringBootApplication
class ClientApplication : CommandLineRunner {
    private val log = logger()
    override fun run(vararg args: String?) = runBlocking {
        try{
            val connector = Connector(0,SimpleClientPacketListener())
            connector.connect("127.0.0.1",30114)

            val apiSvcId="api"
            val roomSvcId = "room"

            ///////// for api ///////////////////////
            var authenticateRes = connector.request(apiSvcId,
                Packet(Simple.AuthenticateReq.newBuilder().setUserId(700).setToken("password").build())
            ).let { AuthenticateRes.parseFrom(it.buffer()) }

            log.info(authenticateRes.userInfo)

            val helloRes = connector.request(apiSvcId,
                Packet(Simple.HelloReq.newBuilder().setMessage("hi!").build())
            ).let { HelloRes.parseFrom(it.buffer()) }

            log.info(helloRes.message)

            connector.send(apiSvcId,
                Packet(SessionUpdateMsg.newBuilder().setSessionInfo("newSessionInfo").build())
            )

            val newSessionInfo = connector.request(apiSvcId,
                Packet(SessionInfoReq.newBuilder().build())
            ).let {Simple.SessionInfoRes.parseFrom(it.buffer()).sessionInfo }

            log.info("sessionInfo:$newSessionInfo")

            connector.send(apiSvcId,Packet(CloseSessionMsg.newBuilder().build()))

            sleep(1000)

            connector.connect("127.0.0.1",30114)

            ///////// for api ///////////////////////
            authenticateRes = connector.request(apiSvcId,
                Packet(Simple.AuthenticateReq.newBuilder().setUserId(700).setToken("password").build())
            ).let { AuthenticateRes.parseFrom(it.buffer()) }

            log.info(authenticateRes.userInfo)

            //////////for  room /////////////////////
            var roomId: Long
            var roomEndpoint: String
            connector.request(apiSvcId,Packet(Simple.CreateRoomReq.newBuilder().setData("success 1").build())
            ).let {
                var res = Simple.CreateRoomRes.parseFrom(it.buffer())
                roomId = res.roomId
                roomEndpoint = res.roomEndpoint
                log.info("${it.msgName} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.request(apiSvcId,Packet(Simple.JoinRoomReq.newBuilder()
                    .setRoomEndpoint(roomEndpoint)
                    .setRoomId(roomId).setData("success 2")
                .build())
            ).let {
                var res = Simple.JoinRoomRes.parseFrom(it.buffer())
                log.info("${it.msgName} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.request(roomSvcId,Packet(Simple.LeaveRoomReq.newBuilder().setData("success 3").build())
            ).let {
                var res = Simple.LeaveRoomRes.parseFrom(it.buffer())
                log.info("${it.msgName} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.request(apiSvcId,Packet(Simple.CreateJoinRoomReq.newBuilder()
                    .setRoomEndpoint(roomEndpoint)
                    .setRoomId(roomId)
                    .setData("success 4")
                .build())
            ).let {
                var res = Simple.CreateJoinRoomRes.parseFrom(it.buffer())
                log.info("${it.msgName} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.send(roomSvcId,Packet(Simple.ChatMsg.newBuilder().setData("hi!").build()))





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