package com.imoonday.client

import com.imoonday.config.*
import com.imoonday.init.*
import com.imoonday.network.*
import com.imoonday.util.*

object AdvancedSkillsClient {

    fun initClient() {
        UIConfig.load()
        UIConfig.initWatchService()
        ModKeyBindings.init()
        Channels.registerClient()
        ModEntities.initClient()
        EventHandler.registerClient()
    }
}