package org.ulalax.playhouse.simple.room

import kotlinx.coroutines.runBlocking
import org.ulalax.playhouse.service.ControlContext.baseSender
import org.ulalax.playhouse.service.ControlContext.systemPanel
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.ulalax.playhouse.communicator.message.Packet
import org.ulalax.playhouse.simple.Simple.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


@Component
class TimeScheduler {
    var log = logger()

    private val dateFormat = SimpleDateFormat("HH:mm:ss")
    val sessionSvcId:Short = 1
    val apiSvcId:Short = 2
    val playSvcId:Short = 3


    @Scheduled(fixedRate = 5000, initialDelay = 10000)
    fun reportCurrentTime() = runBlocking {
//        log.info("The time is now ${dateFormat.format(Date())}")

        try {
          //  var apiInfo = systemPanel.getServerInfoByService(apiSvcId)
            //var res = baseSender.requestToSystem(apiInfo.bindEndpoint(), Packet(HelloReq.newBuilder().setMessage("hello").build()))
            //log.info(HelloRes.parseFrom(res.data()))

        }catch (e:Exception){
            log.error(ExceptionUtils.getStackTrace(e))
        }

    }
}