package com.imoonday.advskills_re.client

import com.imoonday.advskills_re.client.modifier.*
import com.imoonday.advskills_re.client.render.skill.*
import dev.architectury.platform.*

object AdvancedSkillsClient {

    @JvmField
    var clothConfigLoaded: Boolean = Platform.isModLoaded("cloth-config") || Platform.isModLoaded("cloth_config")

    @JvmStatic
    fun initClient() {
        ClientConfig.get().load()
        ModKeyBindings.init()
        SkillRendererHandler.register()
        SkillModifierHandler.register()
        ClientRegistry.register()
    }
}