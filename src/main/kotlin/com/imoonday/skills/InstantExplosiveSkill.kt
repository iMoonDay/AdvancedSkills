package com.imoonday.skills

import com.imoonday.entities.UnstableTntEntity
import com.imoonday.init.ModSounds
import com.imoonday.triggers.VelocitySyncTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.server.network.ServerPlayerEntity

class InstantExplosiveSkill : Skill(
    id = "instant_explosive",
    types = arrayOf(SkillType.DESTRUCTION),
    cooldown = 20,
    rarity = Rarity.EPIC,
    sound = ModSounds.FIRE,
), VelocitySyncTrigger {
    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val rotation = user.velocity.add(rotationVector.normalize().multiply(1.5))
            world.spawnEntity(
                UnstableTntEntity(
                    world,
                    x + rotation.x,
                    eyeY,
                    z + rotation.z,
                    this,
                    rotation
                )
            )
        }
        return UseResult.success()
    }
}