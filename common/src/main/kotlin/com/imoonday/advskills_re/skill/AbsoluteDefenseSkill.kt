package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class AbsoluteDefenseSkill : Skill(
    id = "absolute_defense",
    types = listOf(SkillType.DEFENSE),
    cooldown = 30,
    rarity = Rarity.SUPERB
), DamageTrigger, AutoStopTrigger, UsingRenderTrigger {

    override val persistTime = 30 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean {
        if (!player.isUsing() || amount <= 0) return false
        player.playSound(SoundEvents.ITEM_SHIELD_BLOCK)
        player.stopUsing()
        return true
    }
}
