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

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addEnhancement(ENHANCEMENT_SELF_IMMUNE)
            .addParameter(PARAM_LAUNCH_SOUND, DEFAULT_LAUNCH_SOUND)
            .addParameter(
                name = PARAM_PROJECTILE_COUNT,
                baseValue = DEFAULT_PROJECTILE_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val count = getIntParam(PARAM_PROJECTILE_COUNT, user, DEFAULT_PROJECTILE_COUNT)
            val ignoreSelf = hasEnhancement(ENHANCEMENT_SELF_IMMUNE)
            val sound = getSoundEventParam(PARAM_LAUNCH_SOUND, DEFAULT_LAUNCH_SOUND.get())
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
            }
        ).also {
            if (it && sound != null) {
                playSound(sound)
            }
        }
    }

    override fun isInSpecialState(player: PlayerEntity): Boolean = player.isVulnerable

    companion object {

        // Default Values
        private const val DEFAULT_PROJECTILE_COUNT = 1
        private val DEFAULT_LAUNCH_SOUND = ModSounds.FIRE

        // Parameter Names
        private const val PARAM_LAUNCH_SOUND = "launch_sound"  // 发射音效
        private const val PARAM_PROJECTILE_COUNT = "projectile_count"  // 发射数量

        // Enhancement IDs
        private const val ENHANCEMENT_SELF_IMMUNE = "immune_effect"  // 自身免疫
        private const val ENHANCEMENT_COUNT = "count"  // 对应发射数量
    }
}