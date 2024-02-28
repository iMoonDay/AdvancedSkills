package com.imoonday.skills

import com.imoonday.entities.ServantSkeletonEntity
import com.imoonday.entities.ServantWitherSkeletonEntity
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import kotlin.math.min

class UndeadSummoningSkill : Skill(
    id = "undead_summoning",
    types = arrayOf(SkillType.SUMMON),
    cooldown = 30,
    rarity = Rarity.EPIC,
    sound = SoundEvents.ENTITY_WITHER_SPAWN
) {
    override fun use(user: ServerPlayerEntity): UseResult {
        val count = (1..3).random()
        repeat(count) {
            user.world.spawnEntity(ServantSkeletonEntity(user.world, user))
        }
        repeat((1..min(5 - count, 3)).random()) {
            user.world.spawnEntity(ServantWitherSkeletonEntity(user.world, user))
        }
        return UseResult.success()
    }
}