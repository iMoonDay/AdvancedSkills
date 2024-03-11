package com.imoonday.skill

import com.imoonday.entity.Servant
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.DamageTrigger
import com.imoonday.trigger.UsingRenderTrigger
import com.imoonday.util.SkillSlot
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

class TauntSkill : Skill(
    id = "taunt",
    types = arrayOf(SkillType.ENHANCEMENT),
    cooldown = 30,
    rarity = Rarity.SUPERB,
), DamageTrigger, AutoStopTrigger, UsingRenderTrigger {

    override fun getPersistTime(): Int = 20 * 15

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)
    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing() || attacker !is Servant) amount else amount * 0.75f

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = !player.isUsing()
}