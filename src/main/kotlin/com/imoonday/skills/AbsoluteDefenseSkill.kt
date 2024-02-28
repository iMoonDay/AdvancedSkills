package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.stopUsingSkill
import com.imoonday.triggers.AutoStopTrigger
import com.imoonday.triggers.PlayerDamageTrigger
import com.imoonday.utils.*
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

class AbsoluteDefenseSkill : Skill(
    id = "absolute_defense",
    types = arrayOf(SkillType.DEFENSE),
    cooldown = 30,
    rarity = Rarity.VERY_RARE
), PlayerDamageTrigger, AutoStopTrigger {

    override val persistTime = 20 * 30

    override val skill: Skill
        get() = this

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsingSkill(this)) return amount
        player.world.playSound(null, player.blockPos, SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS)
        player.stopUsingSkill(this)
        return 0.0f
    }
}
