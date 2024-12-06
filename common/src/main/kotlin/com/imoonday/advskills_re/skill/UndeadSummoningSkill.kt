package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.server.network.*
import net.minecraft.sound.*
import kotlin.math.*

class UndeadSummoningSkill : Skill(
    id = "undead_summoning",
    types = listOf(SkillType.SUMMON),
    cooldown = 30,
    rarity = SkillRarity.EPIC,
    sound = SoundEvents::ENTITY_WITHER_SPAWN
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