package org.ulalax.playhouse.simple.api.handler

import org.apache.logging.log4j.kotlin.logger
import org.ulalax.playhouse.service.api.ApiCallBack


class DisconnectApi : ApiCallBack {
    private val log = logger()

    override fun onDisconnect(accountId: Long) {
        log.info("onDisconnect $accountId")
    }
}
