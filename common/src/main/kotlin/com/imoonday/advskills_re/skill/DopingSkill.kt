package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.trigger.renderer.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import kotlin.math.*

class DopingSkill : Skill(
    id = "doping",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 3,
    rarity = Rarity.MYTHIC,
), AttributeTrigger, AutoStopTrigger, UsingRenderTrigger {

    override val persistTime: Int = 10 * 20

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