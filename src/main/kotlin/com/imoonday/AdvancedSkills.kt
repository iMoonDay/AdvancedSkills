package com.imoonday

import com.imoonday.config.Config
import com.imoonday.init.*
import com.imoonday.util.EventHandler
import com.imoonday.util.SkillArgumentType
import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val MOD_ID = "advanced_skills"
val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

object AdvancedSkills : ModInitializer {

    override fun onInitialize() {
        Config.load()
        ModSkills.init()
        ModChannels.registerServer()
        ModCommands.init()
        SkillArgumentType.register()
        ModItemGroups.init()
        ModItems.init()
        ModEffects.init()
        ModSounds.init()
        ModEntities.init()
        ModGameRules.init()
        EventHandler.register()
    }
}