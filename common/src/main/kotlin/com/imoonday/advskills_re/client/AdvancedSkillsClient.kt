package com.imoonday.advskills_re.client

import com.imoonday.advskills_re.client.modifier.*
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.config.*

object AdvancedSkillsClient {

    @JvmStatic
    fun initClient() {
        ClientConfig.get().load()
        ModKeyBindings.init()
        SkillRendererHandler.register()
        SkillModifierHandler.register()
        ClientRegistry.register()
    }
}