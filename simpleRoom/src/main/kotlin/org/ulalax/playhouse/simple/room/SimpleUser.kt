package org.ulalax.playhouse.simple.room

import org.apache.logging.log4j.kotlin.logger
import org.ulalax.playhouse.service.play.Actor
import org.ulalax.playhouse.service.play.ActorSender

class SimpleUser(override val actorSender: ActorSender) : Actor {
    private var log = logger()
    fun accountId():Long{
        return actorSender.accountId()
    }
    override fun onCreate() {
        log.info("onCreate:${actorSender.accountId()}")
    }

    override fun onDestroy() {
        log.info("onDestroy:${actorSender.accountId()}")
    }
}