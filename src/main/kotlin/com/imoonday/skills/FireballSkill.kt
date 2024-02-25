package com.imoonday.skills

import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.entity.projectile.FireballEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents

class FireballSkill : Skill(
    id = "fireball",
    types = arrayOf(SkillType.DESTRUCTION),
    cooldown = 3,
    rarity = Rarity.COMMON,
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