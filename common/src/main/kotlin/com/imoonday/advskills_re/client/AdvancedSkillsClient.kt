package com.imoonday.advskills_re.client

import com.imoonday.advskills_re.config.*

object AdvancedSkillsClient {

    @JvmStatic
    fun initClient() {
        ClientConfig.load()
        ClientConfig.initWatchService()
        ModKeyBindings.init()
        ClientRegistry.register()
    }
}