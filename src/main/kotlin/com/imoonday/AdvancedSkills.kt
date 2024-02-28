package com.imoonday

import com.imoonday.init.*
import com.imoonday.network.Channels
import com.imoonday.skills.Skills
import com.imoonday.utils.EventHandler
import com.imoonday.utils.SkillArgumentType
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

const val MOD_ID = "advanced_skills"

object AdvancedSkills : ModInitializer {
    private val logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        Skills.init()
        Channels.registerServer()
        ModCommands.init()
        SkillArgumentType.register()
        ModItemGroups.init()
        ModItems.init()
        ModEffects.init()
        ModSounds.init()
        ModEntities.init()
        EventHandler.register()
    }
}