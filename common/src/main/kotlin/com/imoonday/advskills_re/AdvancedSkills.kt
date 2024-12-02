package com.imoonday.advskills_re

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.util.*

const val MOD_ID = "advskills_re"

/**
 * TODO
 * 1. 随身箱：随时随地打开末影箱，关闭后冷却5s √
 * 2. 更多功能类技能
 * 3. 易伤技能：发射一个能量球，命中后对周围敌人附加易伤，持续15s
 * 4. 岩浆行者：在岩浆上行走，免疫岩浆块，持续15s √
 * 5. 死亡存档：死亡后可以使用该技能，传送回死亡地点，并高亮掉落物和攻击者(如果有)，并告知消失的物品，冷却5min
 * 6. 回城：蓄力5s后，回到出生点，移动时中断，无冷却 √
 * 7. 背包窥视：查看目标的背包，关闭后冷却30s
 * 8. 凋零：强化三次普攻，附带凋零效果，持续3s，可叠加
 * 9. 技能背包排序：默认、名称、稀有度排序 √
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