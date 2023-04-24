package org.ulalax.playhouse.simple.client

import org.ulalax.playhouse.client.Connector
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.ulalax.playhouse.client.network.message.Packet
import org.ulalax.playhouse.simple.Simple.*
import java.lang.Thread.sleep
import kotlin.system.exitProcess

@SpringBootApplication
class ClientApplication : CommandLineRunner {
    private val log = logger()
    override fun run(vararg args: String?) = runBlocking {
        try{
            val connector = Connector(0,true, SimpleApiPacketListener(),SimpleStagePacketListener())
            connector.connect("127.0.0.1",30114)

            val apiSvcId:Short = 2
            val roomSvcId:Short =3

            ///////// for api ///////////////////////
            var authenticateRes = connector.requestToApi(apiSvcId,
                Packet(AuthenticateReq.newBuilder().setUserId(700).setToken("password").build())
            ).let { AuthenticateRes.parseFrom(it.data()) }

            log.info(authenticateRes.userInfo)

            val helloRes = connector.requestToApi(apiSvcId,
                Packet(HelloReq.newBuilder().setMessage("hi!").build())
            ).let { HelloRes.parseFrom(it.data()) }

            log.info(helloRes.message)

            connector.sendToApi(apiSvcId,
                Packet(SendMsg.newBuilder().setMessage("test send message").build())
            )


            connector.sendToApi(apiSvcId,Packet(CloseSessionMsg.newBuilder().build()))

            sleep(1000)

            connector.connect("127.0.0.1",30114)

            ///////// for api ///////////////////////
            authenticateRes = connector.requestToApi(apiSvcId,
                Packet(AuthenticateReq.newBuilder().setUserId(700).setToken("password").build())
            ).let { AuthenticateRes.parseFrom(it.data()) }

            log.info(authenticateRes.userInfo)

            //////////for  room /////////////////////
            var stageId: Long
            var roomEndpoint: String
            var stageIndex :Int
            connector.requestToApi(apiSvcId,Packet(CreateRoomReq.newBuilder().setData("success 1").build())
            ).let {
                var res = CreateRoomRes.parseFrom(it.data())
                stageId = res.stageId
                roomEndpoint = res.roomEndpoint
                log.info("${it.msgId} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.requestToApi(apiSvcId,Packet(
                JoinRoomReq.newBuilder()
                    .setRoomEndpoint(roomEndpoint)
                    .setRoomId(stageId).setData("success 2")
                .build())
            ).let {
                var res = JoinRoomRes.parseFrom(it.data())
                stageIndex = res.stageIdx
                log.info("${it.msgId} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.requestToStage(roomSvcId,stageIndex,Packet(LeaveRoomReq.newBuilder().setData("success 3").build())
            ).let {
                var res = LeaveRoomRes.parseFrom(it.data())
                log.info("${it.msgId} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.requestToApi(apiSvcId,Packet(
                CreateJoinRoomReq.newBuilder()
                    .setRoomEndpoint(roomEndpoint)
                    .setRoomId(stageId)
                    .setData("success 4")
                .build())
            ).let {
                var res = CreateJoinRoomRes.parseFrom(it.data())
                log.info("${it.msgId} res: error code:${it.errorCode}, data:${res.data}")
            }

            connector.sendToApi(roomSvcId,Packet(ChatMsg.newBuilder().setData("hi!").build()))


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