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

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addEnhancement("immune_to_lightning")
            .addParameter("max_distance", 512.0)
            .addParameter(
                name = "summon_amount",
                baseValue = 1,
                enhancementId = "amount",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val maxDistance = getDoubleParam("max_distance", user, 512.0)
        val result = user.raycastBlock(maxDistance)
        if (result.type != HitResult.Type.BLOCK) return UseResult.fail(failedMessage())
    
        val times = getIntParam("summon_amount", user, 1)
        val immuneToLightning = user.hasEnhancement("immune_to_lightning")
        user.executeAndAddTask(5, times) { summonLightning(user, result.pos, immuneToLightning) }

        return UseResult.success()
    }

    private fun summonLightning(user: ServerPlayerEntity, pos: Vec3d, immuneToLightning: Boolean) =
        EntityType.LIGHTNING_BOLT.create(user.world)?.let {
            (it as DamageFilter).setNoDamagePredicate { it is ItemEntity || immuneToLightning && it == user }
            user.world.spawnEntity(it.apply {
                refreshPositionAfterTeleport(pos)
            })
        } ?: false
}