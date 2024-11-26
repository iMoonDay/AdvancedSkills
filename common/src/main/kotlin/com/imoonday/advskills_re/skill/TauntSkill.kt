package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillSlot
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class TauntSkill : Skill(
    id = "taunt",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 30,
    rarity = Rarity.SUPERB,
), DamageTrigger, AutoStopTrigger, UsingRenderTrigger, TauntTrigger {

    override val persistTime: Int = 20 * 15

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)
    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing() || attacker !is Servant) amount else amount * 0.75f

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = !player.isUsing()
}