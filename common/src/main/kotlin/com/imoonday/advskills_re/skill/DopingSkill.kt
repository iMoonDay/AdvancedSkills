package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import kotlin.math.*

class DopingSkill : Skill(
    Settings(
        id = "doping",
        types = listOf(SkillType.ENHANCEMENT),
        cooldown = 3,
        rarity = SkillRarity.MYTHIC
    )
), AttributeTrigger, AutoStopTrigger, UsingRenderTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(
                name = PARAM_DOPING_DURATION,
                baseValue = DEFAULT_DOPING_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_SPEED_BOOST,
                baseValue = DEFAULT_SPEED_BOOST,
                enhancementId = ENHANCEMENT_SPEED,
                value = 0.05,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_HEALTH_COST,
                baseValue = DEFAULT_HEALTH_COST,
                enhancementId = ENHANCEMENT_COST,
                value = -0.16f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Doping"),
            "Doping",
            getDoubleParam(PARAM_SPEED_BOOST, player, DEFAULT_SPEED_BOOST, 0.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this) {
        user.addAttributes()
        val cost = getFloatParam(PARAM_HEALTH_COST, user, DEFAULT_HEALTH_COST)
        user.health = max(user.health - cost, 1f)
        user.playSound(ModSounds.DASH.get())
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        super.tick(player, usedTime)
        if (player.isUsing() && !player.isSprinting) {
            player.isSprinting = true
        }
    }

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_DOPING_DURATION, player, DEFAULT_DOPING_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        player.removeAttributes()
        player.startCooling()
        super.onStop(player)
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AttributeTrigger>.postUnequipped(player, slot)
        super<AutoStopTrigger>.postUnequipped(player, slot)
    }

    companion object {

        // Default Values
        private const val DEFAULT_DOPING_DURATION = 10 * 20
        private const val DEFAULT_SPEED_BOOST = 0.5
        private const val DEFAULT_HEALTH_COST = 5f

        // Parameter Names
        private const val PARAM_DOPING_DURATION = "doping_duration"  // 兴奋时长
        private const val PARAM_SPEED_BOOST = "speed_boost"  // 速度提升
        private const val PARAM_HEALTH_COST = "health_cost"  // 生命消耗

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应兴奋时长
        private const val ENHANCEMENT_SPEED = "speed"  // 对应速度提升
        private const val ENHANCEMENT_COST = "cost"  // 对应生命消耗
    }
}