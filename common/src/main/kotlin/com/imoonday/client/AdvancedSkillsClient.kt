package com.imoonday.client

import com.imoonday.config.*
import com.imoonday.init.*
import com.imoonday.util.*

object AdvancedSkillsClient {

    @JvmStatic
    fun initClient() {
        UIConfig.load()
        UIConfig.initWatchService()
        ModKeyBindings.init()
        ModEntities.initClient()
        ModParticleTypes.initClient()
        EventHandler.registerClient()
    }
}