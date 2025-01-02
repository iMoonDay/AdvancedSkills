package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.server.network.*
import net.minecraft.sound.*
import kotlin.math.*

class UndeadSummoningSkill : Skill(
    Settings(
        id = "undead_summoning",
        types = listOf(SkillType.SUMMON),
        cooldown = 30,
        rarity = SkillRarity.EPIC
    )
) {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("use_sound", SoundEvents.ENTITY_WITHER_SPAWN)
            .addParameter(
                name = "base_max_amount",
                baseValue = 5,
                enhancementId = "amount",
                value = 2,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val totalMaxAmount = getIntParam("base_max_amount", user, 5)
        val skeletonMaxAmount = floor(totalMaxAmount / 2.0).toInt()
        val count = (1..skeletonMaxAmount).random()
        
        repeat(count) {
            user.world.spawnEntity(ServantSkeletonEntity(user.world, user))
        }
        repeat((1..min(totalMaxAmount - count, skeletonMaxAmount)).random()) {
            user.world.spawnEntity(ServantWitherSkeletonEntity(user.world, user))
        }
        
        return UseResult.success(sound = getSoundEventParam("use_sound", SoundEvents.ENTITY_WITHER_SPAWN))
    }
}