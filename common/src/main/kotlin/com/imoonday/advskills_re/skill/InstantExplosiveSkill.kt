package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.server.network.*

class InstantExplosiveSkill : Skill(
    id = "instant_explosive",
    types = listOf(SkillType.DESTRUCTION),
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