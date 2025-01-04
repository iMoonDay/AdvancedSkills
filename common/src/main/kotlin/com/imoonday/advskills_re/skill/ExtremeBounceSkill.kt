package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class ExtremeBounceSkill : BounceSkill(
    Settings(
        id = "extreme_bounce",
        cooldown = 3,
        rarity = SkillRarity.RARE
    )
) {

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean {
        if (!player.isUsing()) return false
        player.stopUsing()
        player.stopCooling()
        return player.bounce(attacker as? LivingEntity, amount)
    }

    override fun getDuration(): Int = 5

    override fun getDamageMultiplier(): Float = 1f

    override fun getBaseChance(): Float = 0.75f
}