package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.attribute.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class ChargedDashSkill : LongPressSkill(
    id = "charged_dash",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 15,
    rarity = Rarity.SUPERB,
), AttributeTrigger {

    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Charged Dash Charging"),
            "Charged Dash Charging",
            -0.2,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun getMaxPressTime(): Int = 20 * 3

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.addAttributes()
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.run {
            stopUsing()
            removeAttributes()
            velocityDirty = true
            velocity = rotationVector.normalize().multiply(2.0 * pressedTime / getMaxPressTime())
            sendPacket(EntityVelocityUpdateS2CPacket(this))
            spawnParticles(ParticleTypes.CLOUD, false, pos, 10, 0.5, 0.0, 0.5, 0.1)
        }
        return UseResult.success()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)
}