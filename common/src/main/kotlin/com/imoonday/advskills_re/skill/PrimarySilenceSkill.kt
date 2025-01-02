package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

class PrimarySilenceSkill : Skill(
    Settings(
        id = "primary_silence",
        types = listOf(SkillType.CONTROL),
        cooldown = 20,
        rarity = SkillRarity.SUPERB
    )
) {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("launch_sound", ModSounds.FIRE)
            .addParameter(
                name = "extra_range",
                baseValue = 0,
                enhancementId = "range",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "launch_count",
                baseValue = 1,
                enhancementId = "count",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "ignore_owner",
                baseValue = false,
                enhancementId = "self_immune"
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val extraRange = getIntParam("extra_range", user, 0)
        val launchCount = getIntParam("launch_count", user, 1)
        val ignoreSelf = getBooleanParam("ignore_owner", user, false)
        user.executeAndAddTask(5, launchCount) { user.spawnEnergyBall(extraRange, ignoreSelf) }
        return UseResult.success()
    }

    private fun ServerPlayerEntity.spawnEnergyBall(extraRange: Int, ignoreSelf: Boolean): Boolean {
        val rotation = rotationVector.normalize().multiply(1.5)
        return world.spawnEntity(
            SilenceEnergyBallEntity(
                this,
                rotationVector.x,
                rotationVector.y,
                rotationVector.z,
                world
            ).apply {
                setPosition(x + rotation.x, eyeY, z + rotation.z)
                range += extraRange
                if (ignoreSelf) {
                    ignoreOwner = true
                }
            }
        ).also {
            if (it) {
                playSoundFromParam("launch_sound", ModSounds.FIRE.get())
            }
        }
    }
}