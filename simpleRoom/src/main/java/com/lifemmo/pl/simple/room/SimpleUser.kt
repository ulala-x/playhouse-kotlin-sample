package com.lifemmo.pl.simple.room

import com.lifemmo.pl.base.service.room.User
import com.lifemmo.pl.base.service.room.UserSender
import org.apache.logging.log4j.kotlin.logger

class SimpleUser(override val userSender: UserSender) : User {
    private var log = logger()
    fun accountId():Long{
        return userSender.accountId()
    }
    override fun onCreate() {
        log.info("onCreate:${userSender.accountId()}")
    }

    override fun onDestroy() {
        log.info("onDestroy:${userSender.accountId()}")
    }
}