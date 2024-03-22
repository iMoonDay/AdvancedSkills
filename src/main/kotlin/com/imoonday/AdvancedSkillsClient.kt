package com.imoonday

import com.imoonday.config.UIConfig
import com.imoonday.init.ModEntities
import com.imoonday.init.ModKeyBindings
import com.imoonday.network.Channels
import com.imoonday.util.EventHandler
import net.fabricmc.api.ClientModInitializer

object AdvancedSkillsClient : ClientModInitializer {

    override fun onInitializeClient() {
        UIConfig.load()
        UIConfig.initWatchService()
        ModKeyBindings.init()
        Channels.registerClient()
        ModEntities.initClient()
        EventHandler.registerClient()
    }
}