package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.trigger.renderer.*
import com.imoonday.advskills_re.util.SkillSlot
import com.imoonday.advskills_re.util.SkillType
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
    rarity = Rarity.SUPERB,
), DamageTrigger, AttributeTrigger, UsingRenderTrigger {

    override fun getMaxPressTime(): Int = 10 * 10
    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Active Defense"),
            "Active Defense",
            -0.5,
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
        if (pressedTime.toFloat() / getMaxPressTime() < 0.5f) {
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
    ): Float = if (!player.isUsing()) amount else amount * 0.8f

    override fun shouldRenderFeature(target: PlayerEntity, player: PlayerEntity): Boolean =
        target.isUsing() && !target.isUsing(Skills.ABSOLUTE_DEFENSE)
}