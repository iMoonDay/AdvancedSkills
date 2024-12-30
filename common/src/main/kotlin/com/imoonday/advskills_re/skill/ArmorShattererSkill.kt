package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class ArmorShattererSkill : Skill(
    Settings(
        id = "armor_shatterer",
        types = listOf(SkillType.ATTACK),
        cooldown = 15,
        rarity = SkillRarity.EPIC
    )
), SpecialStateRenderTrigger {

    init {
        this.settings
            .addEnhancement("immune_effect")
            .addParameter("launch_sound", ModSounds.FIRE)

        addEnhanceableParameter(
            name = "launch_count",
            baseValue = 1,
            enhancementId = "count",
            value = 1,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val count = getIntParam("launch_count", user, 1)
            val ignoreSelf = hasEnhancement("immune_effect")
            val sound = getSoundEventParam("launch_sound", ModSounds.FIRE.get())
            user.executeAndAddTask(5, count) { spawnEnergyBall(ignoreSelf, sound) }
        }
        return UseResult.success()
    }

    private fun ServerPlayerEntity.spawnEnergyBall(ignoreSelf: Boolean, sound: SoundEvent?): Boolean {
        val rotation = rotationVector.normalize().multiply(1.5)
        return world.spawnEntity(
            VulnerableEnergyBallEntity(
                this,
                rotationVector.x,
                rotationVector.y,
                rotationVector.z,
                world
            ).apply {
                setPosition(x + rotation.x, eyeY, z + rotation.z)
                if (ignoreSelf) {
                    ignoreOwner = true
                }
            }.also {
                if (sound != null) {
                    playSound(sound)
                }
            }
        )
    }

    override fun isInSpecialState(player: PlayerEntity): Boolean = player.isVulnerable
}