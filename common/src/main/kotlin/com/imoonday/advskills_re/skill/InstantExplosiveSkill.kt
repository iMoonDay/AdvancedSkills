package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

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
            .addParameter(PARAM_LAUNCH_SOUND, DEFAULT_LAUNCH_SOUND)
            .addParameter(
                name = PARAM_TNT_COUNT,
                baseValue = DEFAULT_TNT_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val launchCount = getIntParam(PARAM_TNT_COUNT, this, DEFAULT_TNT_COUNT)
            val sound = getSoundEventParam(PARAM_LAUNCH_SOUND, DEFAULT_LAUNCH_SOUND.get())
            executeAndAddTask(5, launchCount) { spawnTnt(sound) }
        }
        return UseResult.success()
    }

    private fun ServerPlayerEntity.spawnTnt(sound: SoundEvent?): Boolean {
        val rotation = velocity.add(rotationVector.normalize().multiply(1.5))
        return world.spawnEntity(UnstableTntEntity(world, x + rotation.x, eyeY, z + rotation.z, this, rotation)).also {
            if (it && sound != null) {
                playSound(sound)
            }
        }
    }

    companion object {

        // Default Values
        private const val DEFAULT_TNT_COUNT = 1
        private val DEFAULT_LAUNCH_SOUND = ModSounds.FIRE

        // Parameter Names
        private const val PARAM_LAUNCH_SOUND = "launch_sound"  // 发射音效
        private const val PARAM_TNT_COUNT = "tnt_count"  // TNT数量

        // Enhancement IDs
        private const val ENHANCEMENT_COUNT = "count"  // 对应TNT数量
    }
}