package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

class InstantExplosiveSkill : Skill(
    Settings(
        id = "instant_explosive",
        types = listOf(SkillType.DESTRUCTION),
        cooldown = 20,
        rarity = SkillRarity.EPIC
    )
), SendPlayerVelocityTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("fire_sound", ModSounds.FIRE)
            .addParameter(
                name = "launch_count",
                baseValue = 1,
                enhancementId = "count",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val launchCount = getIntParam("launch_count", this, 1)
            executeAndAddTask(5, launchCount) { spawnTnt() }
        }
        return UseResult.success(sound = getSoundEventParam("fire_sound", ModSounds.FIRE.get()))
    }

    private fun ServerPlayerEntity.spawnTnt(): Boolean {
        val rotation = velocity.add(rotationVector.normalize().multiply(1.5))
        return world.spawnEntity(UnstableTntEntity(world, x + rotation.x, eyeY, z + rotation.z, this, rotation))
    }
}