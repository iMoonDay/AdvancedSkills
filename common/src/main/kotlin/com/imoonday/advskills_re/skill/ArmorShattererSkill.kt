package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class ArmorShattererSkill : Skill(
    id = "armor_shatterer",
    types = listOf(SkillType.ATTACK),
    cooldown = 15,
    rarity = SkillRarity.EPIC,
    enhancements = setOf(SkillEnhancements.LAUNCH_COUNT, SkillEnhancements.SELF_IMMUNE)
), SpecialStateRenderTrigger {

    init {
        addEnhancementTooltipWithArg(SkillEnhancements.LAUNCH_COUNT) { it.level }
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val count = user.getEnhancementLvl(SkillEnhancements.LAUNCH_COUNT)
            val ignoreSelf = user.hasEnhancement(SkillEnhancements.SELF_IMMUNE)
            user.executeAndAddTask(5, count) { spawnEnergyBall(ignoreSelf) }
        }
        return UseResult.success()
    }

    private fun ServerPlayerEntity.spawnEnergyBall(ignoreSelf: Boolean): Boolean {
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
                playSound(ModSounds.FIRE.get())
            }
        )
    }

    override fun isInSpecialState(player: PlayerEntity): Boolean = player.isVulnerable
}