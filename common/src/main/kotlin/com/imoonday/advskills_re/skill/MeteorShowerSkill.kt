package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class MeteorShowerSkill : LongPressSkill(
    Settings(
        id = "meteor_shower",
        types = listOf(SkillType.ATTACK, SkillType.DESTRUCTION),
        cooldown = 120,
        rarity = SkillRarity.MYTHIC
    )
//    enhancements = setOf(
//        SkillEnhancements.CHARGE_TIME,
//        SkillEnhancements.CHARGE_SLOWDOWN,
//        SkillEnhancements.SUMMON_AMOUNT,
//        SkillEnhancements.RANGE,
//        SkillEnhancements.POWER,
//        SkillEnhancements.VELOCITY
//    )
), AttributeTrigger, UsingRenderTrigger, DangerTrigger {

    override val timeParamName: String = "charge_time"

    init {
        addEnhanceableParameter(
            name = timeParamName,
            baseValue = 10 * 20,
            enhancementId = "time",
            value = -0.16,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Meteor Shower Charging"),
            "Meteor Shower Charging",
            player.applyChargeSlowdownEnhancement(-0.5),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.addAttributes()
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.removeAttributes()
        player.stopUsing()
        if (pressedTime < getPersistTime(player)) {
            player.startCooling(10)
            return UseResult.fail(failedMessage())
        }
        val targetPos = player.raycast(512.0, 0f, false).pos
        val random = player.random
        val extraAmount = player.getEnhancementLvl(SkillEnhancements.SUMMON_AMOUNT)
        val amount = (5..10).random() + extraAmount * 2
        val range = 10 + player.getEnhancementLvl(SkillEnhancements.RANGE) * 2
        val radiusMultiplier = 1f + player.getEnhancementLvl(SkillEnhancements.POWER) * 0.2f
        val velocityMultiplier = 1.0 + player.getEnhancementLvl(SkillEnhancements.VELOCITY) * 0.2
        for (i in 0 until amount) {
            val x = targetPos.x + random.nextDouble() * range * 2 - range
            val z = targetPos.z + random.nextDouble() * range * 2 - range
            val r = (random.nextFloat() + 0.5f) * radiusMultiplier
            player.world.spawnEntity(
                MeteoriteEntity(player.world, Vec3d(x, player.world.topY + r * 2.0, z), r, player).apply {
                    velocity = Vec3d(
                        random.nextDouble() * 0.2 - 0.1,
                        -2.0 * velocityMultiplier,
                        random.nextDouble() * 0.2 - 0.1
                    )
                }
            )
        }
        return UseResult.success()
    }

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean {
        if (player.isUsing()) player.startCooling(10)
        return true
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)
}