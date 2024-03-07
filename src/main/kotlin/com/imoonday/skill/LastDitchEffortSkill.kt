package com.imoonday.skill

import com.imoonday.component.isCooling
import com.imoonday.trigger.*
import com.imoonday.util.SkillSlot
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class LastDitchEffortSkill : Skill(
    id = "last_ditch_effort",
    types = arrayOf(SkillType.PASSIVE),
    cooldown = 180,
    rarity = Rarity.SUPERB,
), DamageTrigger, AutoStopTrigger, AttackTrigger, AttributeTrigger, AutoTrigger, DeathTrigger {
    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            MOVEMENT_SPEED_UUID,
            "Last Ditch Effort",
            0.4,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AutoStopTrigger>.postUnequipped(player, slot)
        super<AttributeTrigger>.postUnequipped(player, slot)
    }

    override fun getPersistTime(): Int = 20 * 15

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        target: LivingEntity,
    ): Float = if (!player.isUsing()) amount else amount + 1

    override fun shouldStart(player: ServerPlayerEntity): Boolean {
        return if (!player.isCooling(this) && (player.health / player.maxHealth) < 0.3f) {
            player.health = player.maxHealth * 0.5f
            player.addAttributes()
            true
        } else false
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing()) amount else amount + 1

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling()
        player.removeAttributes()
        super.onStop(player)
    }

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        player.startCooling()
        return true
    }

    companion object {
        @JvmField
        val MOVEMENT_SPEED_UUID: UUID = UUID.fromString("2B39D07A-41B8-4E8E-8536-2E754CF3D5E2")
    }
}