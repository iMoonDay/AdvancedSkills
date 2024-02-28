package com.imoonday

import com.imoonday.config.Config
import com.imoonday.init.ModEntities
import com.imoonday.init.ModKeyBindings
import com.imoonday.network.Channels
import com.imoonday.utils.EventHandler
import net.fabricmc.api.ClientModInitializer

object AdvancedSkillsClient : ClientModInitializer {
    override fun onInitializeClient() {
        Config.load()
        ModKeyBindings.init()
        Channels.registerClient()
        ModEntities.initClient()
        EventHandler.registerClient()
    }
}