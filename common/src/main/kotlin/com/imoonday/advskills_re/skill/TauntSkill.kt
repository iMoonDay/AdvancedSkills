package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class TauntSkill : Skill(
    Settings(
        id = "taunt",
        types = listOf(SkillType.UTILITY, SkillType.DEFENSE),
        cooldown = 30,
        rarity = SkillRarity.UNCOMMON
    )
), DamageTrigger, AutoStopTrigger, UsingRenderTrigger, TauntTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = timeParamName,
                baseValue = 15 * 20,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "damage_reduction",
                baseValue = 0.25f,
                enhancementId = "defense",
                value = 0.1f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float =
        if (!player.isUsing() || attacker !is Servant) amount
        else amount * (1f - getFloatParam("damage_reduction", player, 0.25f, max = 1.0f))

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = !player.isUsing()

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}