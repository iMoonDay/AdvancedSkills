package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*

class NightVisionSkill : PassiveSkill(
    Settings(
        id = "night_vision",
        rarity = SkillRarity.SUPERB
    ), toggleable = true
), PersistentTrigger, NightVisionTrigger {

    override fun hasNightVision(player: PlayerEntity): Boolean = isAvailable(player)
}