package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.modifyCooldown
import com.imoonday.components.startCooling
import com.imoonday.components.stopUsingSkill
import com.imoonday.trigger.AttributeTrigger
import com.imoonday.trigger.PlayerDamageTrigger
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class ActiveDefenseSkill : LongPressSkill(
    id = "active_defense",
    types = arrayOf(SkillType.DEFENSE),
    cooldown = 10,
    rarity = Rarity.VERY_RARE,
), PlayerDamageTrigger, AttributeTrigger {
    override val maxPressTime: Int = 10 * 10
    override val attribute: Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            MOVEMENT_SPEED_UUID,
            "Active Defense",
            -0.5,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun onPress(player: ServerPlayerEntity): UseResult {
        addAttributes(player)
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsingSkill(this)
        player.startCooling(this)
        if (pressedTime.toFloat() / maxPressTime < 0.5f) {
            player.modifyCooldown(skill) { it / 2 }
        }
        removeAttributes(player)
        return UseResult.consume()
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsingSkill(this)) return amount
        return amount * 0.8f
    }

    companion object {
        val MOVEMENT_SPEED_UUID: UUID = UUID.fromString("A0F548ED-6E81-45D3-83CD-4C2A2FE0A1DC")
    }
}