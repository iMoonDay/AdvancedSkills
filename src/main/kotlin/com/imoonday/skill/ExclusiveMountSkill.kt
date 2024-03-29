package com.imoonday.skill

import com.imoonday.component.properties
import com.imoonday.entity.SpecialTameHorseEntity
import com.imoonday.component.Components
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.translateSkill
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents

class ExclusiveMountSkill : Skill(
    id = "exclusive_mount",
    types = listOf(SkillType.SUMMON),
    cooldown = 60,
    rarity = Rarity.EPIC,
    sound = SoundEvents.ENTITY_HORSE_SADDLE
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        if (!user.isOnGround && !user.isTouchingWater || user.hasVehicle()) return UseResult.fail(
            translateSkill(
                "exclusive_mount",
                "unsupportedStatus"
            )
        )
        val newHorse = SpecialTameHorseEntity(user.world, user).apply {
            initAttributes(random)
            yaw = user.yaw
            pitch = user.pitch
            bodyYaw = user.bodyYaw
            headYaw = user.headYaw
        }
        if (user.world.isSpaceEmpty(newHorse, newHorse.boundingBox.offset(user.pos.subtract(newHorse.pos)))) {
            if (user.properties.containsUuid("horseUuid")) {
                (user.world as ServerWorld).getEntity(user.properties.getUuid("horseUuid"))?.discard()
            }
            user.properties.putUuid("horseUuid", newHorse.uuid)
            Components.PROPERTY.sync(user)
            newHorse.requestTeleport(user.x, user.y, user.z)
            user.world.spawnEntity(newHorse)
            newHorse.putPlayerOnBack(user)
            user.startCooling()
            return UseResult.consume()
        } else return UseResult.fail(translateSkill("exclusive_mount", "unsupportedPlace"))
    }
}