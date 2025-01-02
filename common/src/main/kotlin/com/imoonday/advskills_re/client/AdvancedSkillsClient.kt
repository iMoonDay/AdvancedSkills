package com.imoonday.advskills_re.client

import com.imoonday.advskills_re.client.modifier.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.init.*
import dev.architectury.platform.*
import net.minecraft.client.resource.language.*

object AdvancedSkillsClient {

    @JvmStatic
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