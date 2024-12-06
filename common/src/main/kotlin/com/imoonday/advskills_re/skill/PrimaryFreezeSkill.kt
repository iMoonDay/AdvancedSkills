package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class PrimaryFreezeSkill : Skill(
    id = "primary_freeze",
    types = listOf(SkillType.CONTROL),
    cooldown = 8,
    rarity = SkillRarity.SUPERB,
    sound = ModSounds.FIRE
), SpecialStateRenderTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val rotation = rotationVector.normalize().multiply(1.5)
            world.spawnEntity(
                FreezeEnergyBallEntity(
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

    override fun isInSpecialState(player: PlayerEntity): Boolean = player.isForceFrozen
}