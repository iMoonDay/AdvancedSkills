package com.imoonday

import com.imoonday.config.UIConfigModel
import com.imoonday.init.ModChannels
import com.imoonday.init.ModEntities
import com.imoonday.init.ModKeyBindings
import com.imoonday.util.EventHandler
import net.fabricmc.api.ClientModInitializer

object AdvancedSkillsClient : ClientModInitializer {

    override fun onInitializeClient() {
        UIConfigModel.load()
        ModKeyBindings.init()
        ModChannels.registerClient()
        ModEntities.initClient()
        EventHandler.registerClient()
    }
}