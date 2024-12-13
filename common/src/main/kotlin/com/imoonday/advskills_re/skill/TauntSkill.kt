package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class TauntSkill : Skill(
    id = "taunt",
    types = listOf(SkillType.UTILITY, SkillType.DEFENSE),
    cooldown = 30,
    rarity = SkillRarity.UNCOMMON,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME, SkillEnhancements.DEFENSE_EFFECT)
), DamageTrigger, AutoStopTrigger, UsingRenderTrigger, TauntTrigger {

    override val persistTime: Int = 15 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float =
        if (!player.isUsing() || attacker !is Servant) amount
        else amount * (0.75f - player.getEnhancementLvl(SkillEnhancements.DEFENSE_EFFECT) * 0.1f).coerceAtLeast(0f)

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = !player.isUsing()

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}