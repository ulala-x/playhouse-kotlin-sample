package com.lifemmo.pl.simple.room

import com.lifemmo.pl.base.protocol.Packet
import com.lifemmo.pl.base.service.ControlContext.baseSender
import com.lifemmo.pl.base.service.ControlContext.systemPanel
import com.lifemmo.pl.simple.Simple
import com.lifemmo.pl.simple.Simple.HelloReq
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


@Component
class TimeScheduler {
    var log = logger()

    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    @Scheduled(fixedRate = 5000, initialDelay = 10000)
    fun reportCurrentTime() {
        log.info("The time is now ${dateFormat.format(Date())}")

        try {
            var apiInfo = systemPanel.randomServerInfo("api")
            var res = baseSender.callToSystem(apiInfo.bindEndpoint, Packet(HelloReq.newBuilder().setMessage("hello").build()))
            log.info(Simple.HelloRes.parseFrom(res.buffer()))

        }catch (e:Exception){
            log.error(ExceptionUtils.getStackTrace(e))
        }

    }
}