package com.imoonday.advskills_re

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.util.*

const val MOD_ID = "advskills_re"

/**
 * TODO 凋零：强化三次普攻，附带凋零效果，持续3s，可叠加
 */
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
        Skills.init()
        ModItemGroups.init()
        EventHandler.register()
    }
}