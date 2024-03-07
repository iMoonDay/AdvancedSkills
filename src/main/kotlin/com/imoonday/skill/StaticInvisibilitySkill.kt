package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.SkillType
import net.minecraft.server.network.ServerPlayerEntity

class StaticInvisibilitySkill : PassiveSkill(
    id = "static_invisibility",
    types = arrayOf(SkillType.PASSIVE),
    rarity = Rarity.EPIC,
), AutoTrigger, PersistentTrigger, SendPlayerVelocityTrigger, InvisibilityTrigger {
    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        player.velocity.length() < 0.079 && (player.isOnGround || player.abilities.flying)

    override fun shouldStop(player: ServerPlayerEntity): Boolean = !shouldStart(player)

    override fun getSendTime(): SendPlayerDataTrigger.SendTime = SendPlayerDataTrigger.SendTime.EQUIPPED
}