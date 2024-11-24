package com.imoonday.client

import com.imoonday.config.*

object AdvancedSkillsClient {

    @JvmStatic
    fun initClient() {
        ClientConfig.load()
        ClientConfig.initWatchService()
        ModKeyBindings.init()
        ClientRegistry.register()
    }
}