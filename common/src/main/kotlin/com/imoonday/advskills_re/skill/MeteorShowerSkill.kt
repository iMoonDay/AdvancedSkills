package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillSlot
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.entity.attribute.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class MeteorShowerSkill : LongPressSkill(
    id = "meteor_shower",
    types = listOf(SkillType.ATTACK, SkillType.DESTRUCTION),
    cooldown = 120,
    rarity = Rarity.MYTHIC,
), AttributeTrigger, UsingRenderTrigger {

    override fun getMaxPressTime(): Int = 20 * 10

    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Meteor Shower Charging"),
            "Meteor Shower Charging",
            -0.5,
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
        if (pressedTime < getMaxPressTime()) {
            player.startCooling(10)
            return UseResult.fail(failedMessage())
        }
        val targetPos = player.raycast(512.0, 0f, false).pos
        val random = player.random
        for (i in 0 until (5..10).random()) {
            val x = targetPos.x + random.nextDouble() * 20 - 10
            val z = targetPos.z + random.nextDouble() * 20 - 10
            val r = random.nextFloat() + 0.5f
            player.world.spawnEntity(
                MeteoriteEntity(player.world, Vec3d(x, player.world.topY + r * 2.0, z), r, player).apply {
                    velocity = Vec3d(random.nextDouble() * 0.2 - 0.1, -2.0, random.nextDouble() * 0.2 - 0.1)
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

    override fun isDangerous(player: ServerPlayerEntity): Boolean = player.isUsing()
}