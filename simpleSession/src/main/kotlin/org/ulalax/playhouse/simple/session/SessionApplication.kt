package org.ulalax.playhouse.pl.simple.session

import org.ulalax.playhouse.communicator.CommonOption
import org.ulalax.playhouse.service.session.SessionOption
import org.ulalax.playhouse.service.session.SessionServer
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.ulalax.playhouse.protocol.Server.AuthenticateMsg
import org.ulalax.playhouse.simple.Simple
import org.ulalax.playhouse.simple.Simple.*
import org.ulalax.playhouse.simple.session.SessionSystem
import kotlin.system.exitProcess

@SpringBootApplication
class SessionApplication : CommandLineRunner {
    private val log = logger()
    override fun run(vararg args: String?) {
        try{
            val redisPort = 6379

            val sessionSvcId:Short = 1
            val apiSvcId:Short = 2
            val playSvcId:Short = 3

            val commonOption = CommonOption().apply {
                this.port = 30370
                this.serviceId = sessionSvcId
                this.redisPort = redisPort
                this.serverSystem = {systemPanel,sender -> SessionSystem(systemPanel,sender) }
                this.requestTimeoutSec = 0
            }

            val sessionOption = SessionOption().apply {
                this.sessionPort = 30114
                this.clientSessionIdleTimeout = 0
                this.useWebSocket = true
                this.urls = arrayListOf("$apiSvcId:${AuthenticateReq.getDescriptor().index}")
            }

            val sessionServer = SessionServer(commonOption,sessionOption)

            sessionServer.start()

            Runtime.getRuntime().addShutdownHook(object:Thread(){
                override fun run() {
                    log.info("*** shutting down Session server since JVM is shutting down")
                    sessionServer.stop()
                    log.info("*** server shut down")
                    sleep(1000)
                }
            })

            log.info("Session Server Started")
            sessionServer.awaitTermination()

        }catch (e:Exception){
            log.error(ExceptionUtils.getStackTrace(e))
            exitProcess(1)
        }

    }

}

fun main(args:Array<String>) {
    runApplication<SessionApplication>(*args)

}