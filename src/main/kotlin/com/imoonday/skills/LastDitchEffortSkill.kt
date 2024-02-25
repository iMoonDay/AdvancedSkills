package com.imoonday.skills

import com.imoonday.components.isCooling
import com.imoonday.components.isUsingSkill
import com.imoonday.components.startCooling
import com.imoonday.trigger.*
import com.imoonday.utils.*
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
    rarity = Rarity.VERY_RARE,
), PlayerDamageTrigger, AutoStopTrigger, AttackTrigger, AttributeTrigger, AutoTrigger, DeathTrigger {
    override val attribute: Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            MOVEMENT_SPEED_UUID,
            "Last Ditch Effort",
            0.4,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override val persistTime: Int = 20 * 15
    override val skill: Skill
        get() = this

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        attacker: ServerPlayerEntity,
        entity: LivingEntity,
    ): Float = if (!attacker.isUsingSkill(this)) amount else amount + 1

    override fun shouldStart(player: ServerPlayerEntity): Boolean {
        return if (!player.isCooling(this) && (player.health / player.maxHealth) < 0.3f) {
            player.health = player.maxHealth * 0.5f
            addAttributes(player)
            true
        } else false
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsingSkill(this)) amount else amount + 1

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling(this)
        removeAttributes(player)
        super.onStop(player)
    }

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        player.startCooling(this)
        return true
    }

    companion object {
        @JvmField
        val MOVEMENT_SPEED_UUID: UUID = UUID.fromString("2B39D07A-41B8-4E8E-8536-2E754CF3D5E2")
    }
}