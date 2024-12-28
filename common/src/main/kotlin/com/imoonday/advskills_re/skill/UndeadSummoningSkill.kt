package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
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
    sound = SoundEvents::ENTITY_WITHER_SPAWN,
    enhancements = setOf(SkillEnhancements.SUMMON_AMOUNT)
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val extraAmount = user.getEnhancementLvl(SkillEnhancements.SUMMON_AMOUNT) * 2
        val totalMaxAmount = 5 + extraAmount
        val skeletonMaxAmount = floor(totalMaxAmount / 2.0).toInt()
        val count = (1..skeletonMaxAmount).random()
        repeat(count) {
            user.world.spawnEntity(ServantSkeletonEntity(user.world, user))
        }
        repeat((1..min(totalMaxAmount - count, skeletonMaxAmount)).random()) {
            user.world.spawnEntity(ServantWitherSkeletonEntity(user.world, user))
        }
        return UseResult.success()
    }
}