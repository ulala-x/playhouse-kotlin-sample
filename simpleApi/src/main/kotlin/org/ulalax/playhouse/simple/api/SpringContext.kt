package org.ulalax.playhouse.simple.api

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringContext : ApplicationContextAware {
    companion object{
        private lateinit var context: ApplicationContext
        fun getContext(): ApplicationContext {
            return context
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext;
    }

}