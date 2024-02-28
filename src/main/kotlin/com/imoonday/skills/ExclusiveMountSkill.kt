package com.imoonday.skills

import com.imoonday.components.startCooling
import com.imoonday.components.status
import com.imoonday.entities.SpecialTameHorseEntity
import com.imoonday.init.ModComponents
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import com.imoonday.utils.translateSkill
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents

class ExclusiveMountSkill : Skill(
    id = "exclusive_mount",
    types = arrayOf(SkillType.SUMMON),
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
            if (user.status.containsUuid("horseUuid")) {
                (user.world as ServerWorld).getEntity(user.status.getUuid("horseUuid"))?.discard()
            }
            user.status.putUuid("horseUuid", newHorse.uuid)
            ModComponents.STATUS.sync(user)
            newHorse.requestTeleport(user.x, user.y, user.z)
            user.world.spawnEntity(newHorse)
            newHorse.putPlayerOnBack(user)
            user.startCooling(this)
            return UseResult.consume()
        } else return UseResult.fail(translateSkill("exclusive_mount", "unsupportedPlace"))
    }
}