package com.imoonday.skill

import com.imoonday.component.resetSkillUsedTime
import com.imoonday.init.ModSounds
import com.imoonday.trigger.AttributeTrigger
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.DamageTrigger
import com.imoonday.util.SkillSlot
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.playSound
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.Monster
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.entity.projectile.thrown.PotionEntity
import net.minecraft.potion.PotionUtil
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*
import java.util.function.Predicate

class DangerPerceptionSkill : Skill(
    id = "danger_perception",
    types = arrayOf(SkillType.PASSIVE),
    cooldown = 12,
    rarity = Rarity.SUPERB,
), AutoStopTrigger, AttributeTrigger, DamageTrigger {
    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override fun getPersistTime(): Int = 20 * 2
    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            MOVEMENT_SPEED_UUID,
            "Danger Perception",
            0.3,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AttributeTrigger>.postUnequipped(player, slot)
        super<AutoStopTrigger>.postUnequipped(player, slot)
    }

    override fun tick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isCreative && !player.isSpectator && (player.isUsing() || !player.isCooling())) {
            val hasDanger = player.world
                .getOtherEntities(player, player.boundingBox.expand(3.0), getDangerPredicate(player))
                .isNotEmpty()
            if (hasDanger) {
                if (player.isUsing()) player.resetSkillUsedTime(this) else start(player)
            }
        }
        super.tick(player, usedTime)
    }

    override fun onStop(player: ServerPlayerEntity) {
        player.removeAttributes()
        super.onStop(player)
    }

    private fun getDangerPredicate(player: ServerPlayerEntity): Predicate<Entity> =
        Predicate<Entity> { it is ProjectileEntity && !it.isOnGround && it.owner != player }
            .or { it is Monster && it.isAlive }
            .or { it is HostileEntity && it.isAlive && it.target == player }
            .or {
                it is PotionEntity &&
                        (PotionUtil.getPotion(it.stack).effects
                            .any { !it.effectType.isBeneficial }
                                || PotionUtil.getPotionEffects(it.stack)
                            .any { !it.effectType.isBeneficial })
            }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsing() || player.isCooling()) return amount
        if (source.source != null || source.attacker != null) {
            if (source.source != null && source.source == player) return amount
            if (source.attacker != null && source.attacker == player) return amount
            start(player)
        }
        return amount
    }

    private fun start(player: ServerPlayerEntity) {
        player.playSound(ModSounds.DASH)
        player.startUsing()
        player.startCooling()
        player.addAttributes()
    }

    companion object {
        @JvmField
        val MOVEMENT_SPEED_UUID: UUID = UUID.fromString("A1452D15-3B39-2A1D-205C-6FD6B9BEECC9")
    }
}