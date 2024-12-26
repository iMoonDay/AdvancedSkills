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
    id = "doping",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 3,
    rarity = SkillRarity.MYTHIC,
    enhancements = setOf(
        SkillEnhancements.PERSISTENT_TIME,
        SkillEnhancements.MOVEMENT_SPEED,
        SkillEnhancements.USE_COST
    )
), AttributeTrigger, AutoStopTrigger, UsingRenderTrigger {

    init {
        addEnhanceableParameter(timeParameterName, 10 * 20, "time", 0.2f, Enhancement.Type.MULTIPLY, 5) { (it * 100).toInt() }
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Doping"),
            "Doping",
            0.5 + player.getEnhancementLvl(SkillEnhancements.MOVEMENT_SPEED) * 0.05,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun use(user: ServerPlayerEntity): UseResult {
        val result = UseResult.startUsing(user, this)
        if (!result.success) return result
        user.addAttributes()
        val cost = 5f * (1 - user.getEnhancementLvl(SkillEnhancements.USE_COST) * 0.16f)
        user.health = max(user.health - cost, 1f)
        user.playSound(ModSounds.DASH.get())
        return result
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        super.tick(player, usedTime)
        if (player.isUsing() && !player.isSprinting) {
            player.isSprinting = true
        }
    }

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