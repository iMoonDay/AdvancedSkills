package com.imoonday

import com.imoonday.config.*
import com.imoonday.init.*
import com.imoonday.network.*
import com.imoonday.skill.*
import com.imoonday.util.*
import com.mojang.logging.*
import org.slf4j.*

const val MOD_ID = "advanced_skills_re"
val LOGGER: Logger = LogUtils.getLogger()

object AdvancedSkills {

    fun init() {
        Config.load()
        Skills.init()
        Channels.register()
        ModCommands.init()
        SkillArgumentType.register()
        ModItemGroups.init()
        ModItems.init()
        ModBlocks.init()
        ModEffects.init()
        ModSounds.init()
        ModEntities.init()
        ModGameRules.init()
        EventHandler.register()
    }
}