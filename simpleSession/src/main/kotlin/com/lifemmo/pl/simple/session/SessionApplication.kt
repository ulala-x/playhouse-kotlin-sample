package com.lifemmo.pl.simple.session

import com.lifemmo.pl.base.communicator.CommonOption
import com.lifemmo.pl.base.service.session.SessionOption
import com.lifemmo.pl.base.service.session.SessionServer
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication
class SessionApplication : CommandLineRunner {
    private val log = logger()
    override fun run(vararg args: String?) {
        try{
            val redisPort = 6379

            val commonOption = CommonOption().apply {
                this.port = 30370
                this.serviceId = "session"
                this.redisPort = redisPort
                this.serverSystem = {baseSender,systemSender -> SessionSystem(systemSender,baseSender)}
                this.requestTimeoutSec = 0
            }

            val sessionOption = SessionOption().apply {
                this.sessionPort = 30114
                this.clientSessionIdleTimeout = 0
                this.urls = arrayListOf("api:AuthenticateReq")
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