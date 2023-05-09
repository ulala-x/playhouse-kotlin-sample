package org.ulalax.playhouse.simple.room

import ConsoleLogger
import org.ulalax.playhouse.communicator.CommonOption
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.ulalax.playhouse.service.play.PlayOption
import org.ulalax.playhouse.service.play.PlayServer
import kotlin.system.exitProcess

@EnableScheduling
@SpringBootApplication
class PlayApplication : CommandLineRunner {
    private val log = logger()
    override fun run(vararg args: String?) {
        try{
            val redisPort = 6379

            val commonOption = CommonOption().apply {
                this.port = 30570
                this.serviceId = 3
                this.redisPort = redisPort
                this.serverSystem = {systemPanel,sender -> PlaySystem(systemPanel,sender) }
                this.logger = ConsoleLogger()
                this.requestTimeoutSec = 0
            }

            val playOption = PlayOption().apply {
                this.elementConfigurator.register("simple",
                    {stageSender -> SimpleRoom(stageSender) },{ userSender -> SimpleUser(userSender) })
            }

            val playServer = PlayServer(commonOption,playOption)

            playServer.start()

            Runtime.getRuntime().addShutdownHook(object:Thread(){
                override fun run() {
                    log.info("*** shutting down Room server since JVM is shutting down")
                    playServer.stop()
                    log.info("*** server shutdown")
                    sleep(1000)
                }
            })

            log.info("Room Server Started")
            playServer.awaitTermination()

        }catch (e:Exception){
            log.error(ExceptionUtils.getStackTrace(e))
            exitProcess(1)
        }

    }

}

fun main(args:Array<String>) {
    runApplication<PlayApplication>(*args)
}