package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.server.network.*
import net.minecraft.util.hit.*
import net.minecraft.util.math.*

class ThunderFurySkill : Skill(
    Settings(
        id = "thunder_fury",
        types = listOf(SkillType.ATTACK),
        cooldown = 15,
        rarity = SkillRarity.EPIC
    )
) {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addEnhancement(ENHANCEMENT_LIGHTNING_IMMUNE)
            .addParameter(PARAM_TARGET_DISTANCE, DEFAULT_TARGET_DISTANCE)
            .addParameter(
                name = PARAM_LIGHTNING_COUNT,
                baseValue = DEFAULT_LIGHTNING_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val maxDistance = getDoubleParam(PARAM_TARGET_DISTANCE, user, DEFAULT_TARGET_DISTANCE)
        val result = user.raycastBlock(maxDistance)
        if (result.type != HitResult.Type.BLOCK) return UseResult.fail(failedMessage)

        val lightningCount = getIntParam(PARAM_LIGHTNING_COUNT, user, DEFAULT_LIGHTNING_COUNT)
        val immuneToLightning = user.hasEnhancement(ENHANCEMENT_LIGHTNING_IMMUNE)
        user.executeAndAddTask(5, lightningCount) { summonLightning(user, result.pos, immuneToLightning) }

        return UseResult.success()
    }

    private fun summonLightning(user: ServerPlayerEntity, pos: Vec3d, immuneToLightning: Boolean) =
        EntityType.LIGHTNING_BOLT.create(user.world)?.let {
            (it as DamageFilter).setNoDamagePredicate { it is ItemEntity || immuneToLightning && it == user }
            user.world.spawnEntity(it.apply {
                refreshPositionAfterTeleport(pos)
            })
        } ?: false

    companion object {

        // Default Values
        private const val DEFAULT_TARGET_DISTANCE = 512.0
        private const val DEFAULT_LIGHTNING_COUNT = 1

        // Parameter Names
        private const val PARAM_TARGET_DISTANCE = "target_distance"  // 目标距离
        private const val PARAM_LIGHTNING_COUNT = "lightning_count"  // 闪电数量

        // Enhancement IDs
        private const val ENHANCEMENT_COUNT = "count"  // 对应数量
        private const val ENHANCEMENT_LIGHTNING_IMMUNE = "lightning_immune"  // 对应闪电免疫
    }
}