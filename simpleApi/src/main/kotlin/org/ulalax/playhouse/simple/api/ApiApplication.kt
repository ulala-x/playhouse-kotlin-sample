package org.ulalax.playhouse.simple.api;

import ConsoleLogger
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.ulalax.playhouse.communicator.CommonOption
import org.ulalax.playhouse.service.api.ApiOption
import org.ulalax.playhouse.service.api.ApiServer
import org.ulalax.playhouse.simple.api.handler.DisconnectApi
import java.lang.Thread.sleep
import java.util.concurrent.Executors
import kotlin.system.exitProcess


@EnableScheduling
@SpringBootApplication
class ApiApplication : CommandLineRunner {
    private val log = logger()
    override fun run(vararg args: String) {
        try {
            log.debug("api start")
            val commonOption = CommonOption()
            commonOption.serverSystem = { systemPanel ,baseSender -> ApiSystem(systemPanel, baseSender) }
            commonOption.port = 30470
            commonOption.serviceId = 2
            commonOption.redisPort = 6379
            commonOption.requestTimeoutSec = 0
            commonOption.logger = ConsoleLogger()
            val apiOption = ApiOption()
            apiOption.apiPath = ApiApplication::class.java.getPackage().name
            apiOption.apiCallBackHandler = DisconnectApi()
            val apiServer = ApiServer(commonOption, apiOption)
            apiServer.start()
            Runtime.getRuntime().addShutdownHook(Thread {
                log.info("*** shutting down Api server since JVM is shutting down")
                apiServer.stop()
                log.info("*** server shut down")
                try {
                    sleep(1000)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            })
            log.info("Api Server Started")
            apiServer.awaitTermination()
        } catch (e: Exception) {
            log.error(ExceptionUtils.getMessage(e))
            exitProcess(1)
        }
    }

}

fun main(args:Array<String>) {
    runApplication<ApiApplication>(*args)
}