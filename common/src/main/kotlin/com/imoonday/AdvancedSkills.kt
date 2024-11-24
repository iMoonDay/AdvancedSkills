package com.imoonday

import com.imoonday.config.*
import com.imoonday.init.*
import com.imoonday.network.*
import com.imoonday.skill.*
import com.imoonday.util.*

const val MOD_ID = "advanced_skills_re"

object AdvancedSkills {

    @JvmStatic
    fun init() {
        SkillConfig.load()
        Channels.register()
        ModCommands.init()
        ModItems.init()
        ModBlocks.init()
        ModEffects.init()
        ModSounds.init()
        ModEntities.init()
        Skills.init()
        ModItemGroups.init()
        EventHandler.register()
    }
}