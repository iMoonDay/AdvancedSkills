package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillType
import net.minecraft.server.network.*

class StaticInvisibilitySkill : PassiveSkill(
    id = "static_invisibility",
    rarity = Rarity.EPIC,
), AutoTrigger, PersistentTrigger, SendPlayerVelocityTrigger, InvisibilityTrigger {

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        player.velocity.length() < 0.079 && (player.isOnGround || player.abilities.flying) && !player.isUsingItem

    override fun shouldStop(player: ServerPlayerEntity): Boolean = !shouldStart(player)

    override fun getSendTime(): SendTime = SendTime.EQUIPPED
}