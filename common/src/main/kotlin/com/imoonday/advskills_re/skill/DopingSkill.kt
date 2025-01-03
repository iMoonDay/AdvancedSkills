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

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = "persist_time",
                baseValue = 10 * 20,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "speed_multiplier",
                baseValue = 0.5,
                enhancementId = "speed",
                value = 0.05,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "health_cost",
                baseValue = 5f,
                enhancementId = "cost",
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
            getDoubleParam("speed_multiplier", player, 0.5, 0.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this) {
        user.addAttributes()
        val cost = getFloatParam("health_cost", user, 5f)
        user.health = max(user.health - cost, 1f)
        user.playSound(ModSounds.DASH.get())
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        super.tick(player, usedTime)
        if (player.isUsing() && !player.isSprinting) {
            player.isSprinting = true
        }
    }

    override fun getMaxUseTime(player: PlayerEntity): Int = getIntParam("persist_time", player, 10 * 20, 0)

    override fun onStop(player: ServerPlayerEntity) {
        player.removeAttributes()
        player.startCooling()
        super.onStop(player)
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AttributeTrigger>.postUnequipped(player, slot)
        super<AutoStopTrigger>.postUnequipped(player, slot)
    }
}