package com.imoonday.skill

import com.imoonday.entity.SilenceEnergyBallEntity
import com.imoonday.init.ModSounds
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.server.network.ServerPlayerEntity

class PrimarySilenceSkill : Skill(
    id = "primary_silence",
    types = arrayOf(SkillType.CONTROL),
    cooldown = 20,
    rarity = Rarity.SUPERB,
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