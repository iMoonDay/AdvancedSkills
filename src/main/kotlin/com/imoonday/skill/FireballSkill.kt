package com.imoonday.skill

import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.entity.projectile.FireballEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents

class FireballSkill : Skill(
    id = "fireball",
    types = arrayOf(SkillType.DESTRUCTION),
    cooldown = 5,
    rarity = Rarity.SUPERB,
    sound = SoundEvents.ENTITY_ENDER_DRAGON_SHOOT
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val rotation = rotationVector.normalize().multiply(1.5)
            world.spawnEntity(
                FireballEntity(
                    world,
                    this,
                    rotation.x,
                    rotation.y,
                    rotation.z,
                    1
                ).apply {
                    setPosition(x + rotation.x, eyeY, z + rotation.z)
                }
            )
        }
        return UseResult.success()
    }
}