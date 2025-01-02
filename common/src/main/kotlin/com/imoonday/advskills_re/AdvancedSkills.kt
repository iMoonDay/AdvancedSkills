package com.imoonday.advskills_re

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.util.*

const val MOD_ID = "advskills_re"

/***
 * TODO:
 * 1. 轻盈落地：可以免疫一次摔落伤害
 * 2. 辟谷：被动，饱食度不会耗尽，始终至少有1点饱食度
 * 3. 巨大化：变大一倍，攻击力和防御力提升，移动速度降低
 */
object AdvancedSkills {

    @JvmStatic
    fun init() {
        GlobalConfig.get().load()
        SkillRarity.init()
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