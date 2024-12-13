package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class ChargedDashSkill : LongPressSkill(
    id = "charged_dash",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 15,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(
        SkillEnhancements.CHARGE_TIME,
        SkillEnhancements.CHARGE_SLOWDOWN,
        SkillEnhancements.VELOCITY
    )
), AttributeTrigger {

    init {
        addEnhancementTooltipWithArg(SkillEnhancements.VELOCITY) { it.level * 20 }
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Charged Dash Charging"),
            "Charged Dash Charging",
            player.applyChargeSlowdownEnhancement(-0.2),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun getMaxPressTime(): Int = 3 * 20

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.addAttributes()
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.run {
            stopUsing()
            removeAttributes()
            val multiplier = 1.0 + player.getEnhancementLvl(SkillEnhancements.VELOCITY) * 0.2
            velocity =
                rotationVector.normalize().multiply(2.0 * pressedTime / getModifiedPersistTime(player) * multiplier)
            updateVelocity()
            spawnParticles(ParticleTypes.CLOUD, false, pos, 10, 0.5, 0.0, 0.5, 0.1)
        }
        return UseResult.success()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)
}