package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillSlot
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.entity.attribute.*
import net.minecraft.server.network.*
import kotlin.math.*

class DopingSkill : Skill(
    id = "doping",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 3,
    rarity = Rarity.MYTHIC,
), AttributeTrigger, AutoStopTrigger, UsingRenderTrigger {

    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Doping"),
            "Doping",
            0.5,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun use(user: ServerPlayerEntity): UseResult {
        val result = UseResult.startUsing(user, this)
        if (!result.success) return result
        user.addAttributes()
        user.health = max(user.health - 5f, 1f)
        return result
    }

    override val persistTime: Int = 20 * 10

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