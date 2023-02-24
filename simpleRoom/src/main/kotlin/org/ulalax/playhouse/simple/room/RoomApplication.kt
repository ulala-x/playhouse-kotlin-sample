package org.ulalax.playhouse.simple.room

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
class RoomApplication : CommandLineRunner {
    private val log = logger()
    override fun run(vararg args: String?) {
        try{
            val redisPort = 6379

            val commonOption = CommonOption().apply {
                this.port = 30570
                this.serviceId = "room"
                this.redisPort = redisPort
                this.serverSystem = {baseSender,systemSender -> PlaySystem(baseSender,systemSender) }
            }

            val playOption = PlayOption().apply {
                this.elementConfigurator.register("simple",
                    {stageSender -> SimpleRoom(stageSender) },{ userSender -> SimpleUser(userSender) })
            }

            val roomServer = PlayServer(commonOption,playOption)

            roomServer.start()

            Runtime.getRuntime().addShutdownHook(object:Thread(){
                override fun run() {
                    log.info("*** shutting down Room server since JVM is shutting down")
                    roomServer.stop()
                    log.info("*** server shutdown")
                    sleep(1000)
                }
            })

            log.info("Room Server Started")
            roomServer.awaitTermination()

        }catch (e:Exception){
            log.error(ExceptionUtils.getStackTrace(e))
            exitProcess(1)
        }

    }

}

fun main(args:Array<String>) {
    runApplication<RoomApplication>(*args)
}