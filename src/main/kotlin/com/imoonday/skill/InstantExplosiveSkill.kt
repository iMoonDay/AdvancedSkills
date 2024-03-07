package com.imoonday.skill

import com.imoonday.entity.UnstableTntEntity
import com.imoonday.init.ModSounds
import com.imoonday.trigger.SendPlayerVelocityTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.server.network.ServerPlayerEntity

class InstantExplosiveSkill : Skill(
    id = "instant_explosive",
    types = arrayOf(SkillType.DESTRUCTION),
    cooldown = 20,
    rarity = Rarity.EPIC,
    sound = ModSounds.FIRE,
), SendPlayerVelocityTrigger {
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