package com.imoonday.advskills_re

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*

const val MOD_ID = "advskills_re"

/**
 * TODO
 * 1.伪装术：伪装成脚底下的方块，持续30s
 */
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