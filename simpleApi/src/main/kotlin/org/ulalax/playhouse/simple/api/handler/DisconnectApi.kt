package org.ulalax.playhouse.simple.api.handler

import lombok.extern.slf4j.Slf4j
import org.apache.logging.log4j.kotlin.logger
import org.ulalax.playhouse.service.api.ApiCallBackHandler

@Slf4j
class DisconnectApi : ApiCallBackHandler {
    private val log = logger()

    override fun onDisconnect(accountId: Long, sessionInfo: String) {
        log.info("onDisconnect $accountId,$sessionInfo")
    }
}
