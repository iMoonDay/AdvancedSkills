package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import net.minecraft.server.network.*

class NightVisionSkill : PassiveSkill(
    id = "night_vision",
    rarity = Rarity.SUPERB,
    toggleable = true
), PersistentTrigger, NightVisionTrigger {

    override fun keepUsingAfterRespawn(player: ServerPlayerEntity): Boolean = true
}