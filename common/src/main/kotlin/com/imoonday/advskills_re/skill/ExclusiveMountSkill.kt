package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class ExclusiveMountSkill : Skill(
    Settings(
        id = "exclusive_mount",
        types = listOf(SkillType.SUMMON),
        cooldown = 60,
        rarity = SkillRarity.EPIC
    )
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        if (!user.isOnGround && !user.isTouchingWater || user.hasVehicle()) {
            return UseResult.fail(message("unsupportedStatus"))
        }
        val newHorse = SpecialTameHorseEntity(user.world, user).apply {
            initAttributes(random)
            yaw = user.yaw
            pitch = user.pitch
            bodyYaw = user.bodyYaw
            headYaw = user.headYaw
        }
        if (user.world.isSpaceEmpty(newHorse, newHorse.boundingBox.offset(user.pos.subtract(newHorse.pos)))) {
            val properties = user.properties
            if (properties.containsUuid(NBT_HORSE_UUID)) {
                user.serverWorld.getEntity(properties.getUuid(NBT_HORSE_UUID))?.discard()
            }
            properties.putUuid(NBT_HORSE_UUID, newHorse.uuid)
            user.syncProperties()
            newHorse.requestTeleport(user.x, user.y, user.z)
            user.world.spawnEntity(newHorse)
            newHorse.putPlayerOnBack(user)
            user.startCooling()
            return UseResult.consume(sound = DEFAULT_MOUNT_SOUND)
        } else return UseResult.fail(message("unsupportedPlace"))
    }

    companion object {

        // NBT Keys
        const val NBT_HORSE_UUID = "HorseUuid"  // 坐骑UUID

        // Default Values
        private val DEFAULT_MOUNT_SOUND = SoundEvents.ENTITY_HORSE_SADDLE  // 坐骑音效
    }
}