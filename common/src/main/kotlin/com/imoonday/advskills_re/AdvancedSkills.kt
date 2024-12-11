package com.imoonday.advskills_re

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.util.*
import dev.architectury.platform.*
import dev.architectury.utils.*
import net.fabricmc.api.*

const val MOD_ID = "advskills_re"

object AdvancedSkills {

    @JvmStatic
    fun init() {
        Channels.register()
        ModCommands.init()
        ModItems.init()
        ModBlocks.init()
        ModEffects.init()
        ModSounds.init()
        ModEntities.init()
        SkillEnhancements.init()
        Skills.init()
        ModItemGroups.init()
        EventHandler.register()
    }
}