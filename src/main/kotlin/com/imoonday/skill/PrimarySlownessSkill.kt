package com.imoonday.skill

import com.imoonday.entity.SlownessEnergyBallEntity
import com.imoonday.init.ModSounds
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.server.network.ServerPlayerEntity

class PrimarySlownessSkill : Skill(
    id = "primary_slowness",
    types = listOf(SkillType.CONTROL),
    cooldown = 6,
    rarity = Rarity.RARE,
    sound = ModSounds.FIRE
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val rotation = rotationVector.normalize().multiply(1.5)
            world.spawnEntity(
                SlownessEnergyBallEntity(
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