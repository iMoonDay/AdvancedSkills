package com.imoonday.skills

import com.imoonday.entities.SilenceEnergyBallEntity
import com.imoonday.init.ModSounds
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.server.network.ServerPlayerEntity

class PrimarySilenceSkill : Skill(
    id = "primary_silence",
    types = arrayOf(SkillType.CONTROL),
    cooldown = 20,
    rarity = Rarity.VERY_RARE,
    sound = ModSounds.FIRE
) {
    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val rotation = rotationVector.normalize().multiply(1.5)
            world.spawnEntity(
                SilenceEnergyBallEntity(
                    this,
                    rotationVector.x,
                    rotationVector.y,
                    rotationVector.z,
                    world
                ).apply {
                    setPosition(x + rotation.x, eyeY, z + rotation.z)
                }
            )
        }
        return UseResult.success()
    }
}