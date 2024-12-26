package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.isUsing
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class ActiveDefenseSkill : LongPressSkill(
    id = "active_defense",
    types = listOf(SkillType.DEFENSE),
    cooldown = 10,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(
        SkillEnhancements.PERSISTENT_TIME,
        SkillEnhancements.DEFENSE_EFFECT,
        SkillEnhancements.CHARGE_SLOWDOWN
    )
), DamageTrigger, AttributeTrigger, UsingRenderTrigger {

    init {
        addEnhanceableParameter(timeParameterName, 5 * 20, "time", 0.2f, Enhancement.Type.MULTIPLY, 5) { (it * 100).toInt() }

        addEnhancementTooltipWithArg(SkillEnhancements.DEFENSE_EFFECT) { it.level * 6 }
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Active Defense"),
            "Active Defense",
            player.applyChargeSlowdownEnhancement(-0.5),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.addAttributes()
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopAndCooldown()
        if (pressedTime.toFloat() / getPersistTime(player) < 0.5f) {
            player.modifyCooldown { it / 2 }
        }
        player.removeAttributes()
        return UseResult.consume()
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing()) amount
    else amount * (0.8f - player.getEnhancementLvl(SkillEnhancements.DEFENSE_EFFECT) * 0.06f)

    override fun shouldRenderFeature(target: PlayerEntity, clientPlayer: PlayerEntity): Boolean =
        target.isUsing() && !target.isUsing(Skills.ABSOLUTE_DEFENSE)
}