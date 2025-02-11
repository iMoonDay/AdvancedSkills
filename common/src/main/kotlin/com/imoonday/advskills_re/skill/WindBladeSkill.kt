package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*
import kotlin.math.*

class WindBladeSkill : Skill(
    Settings(
        id = "wind_blade",
        types = listOf(SkillType.ENHANCEMENT),
        cooldown = 10,
        rarity = SkillRarity.RARE
    )
), PostAttackTrigger, PersistentTrigger, DeathTrigger, UsingRenderTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(
                name = PARAM_TORNADO_SPEED,
                baseValue = DEFAULT_TORNADO_SPEED,
                enhancementId = ENHANCEMENT_SPEED,
                value = 0.05,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_TORNADO_COUNT,
                baseValue = DEFAULT_TORNADO_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postSweepAttack(player: PlayerEntity, target: LivingEntity) {
        super.postSweepAttack(player, target)
        if (!player.isUsing() || player.world.isClient) return
        val tornadoSpeed =
            player.horizontalRotationVector * getDoubleParam(PARAM_TORNADO_SPEED, player, DEFAULT_TORNADO_SPEED)
        val extraTornados = getIntParam(PARAM_TORNADO_COUNT, player, DEFAULT_TORNADO_COUNT)
        spawnTornado(player, tornadoSpeed, target)
        calculateRadians(extraTornados).forEach {
            spawnTornado(player, tornadoSpeed.rotateY(-it.toFloat()), target)
        }
        player.stopAndCooldown()
    }

    private fun calculateRadians(n: Int): List<Double> {
        val negativeCount = floor(n / 2.0).toInt()
        val positiveCount = n - negativeCount

        val radians = mutableListOf<Double>()

        if (negativeCount > 0) {
            for (i in 0 until negativeCount) {
                val r = -(i + 1) * SPREAD_ANGLE / (negativeCount + 1)
                radians.add(r * PI / 180.0)
            }
        }

        if (positiveCount > 0) {
            for (i in 0 until positiveCount) {
                val r = (i + 1) * SPREAD_ANGLE / (positiveCount + 1)
                radians.add(r * PI / 180.0)
            }
        }

        return radians
    }

    private fun spawnTornado(
        player: PlayerEntity,
        velocity: Vec3d,
        target: LivingEntity
    ) {
        player.world.spawnEntity(TornadoEntity(player.world, player, velocity).apply {
            setPosition(target.pos)
        })
    }

    override fun onDeath(player: ServerPlayerEntity, source: DamageSource) {
        super.onDeath(player, source)
        if (player.isUsing()) {
            player.startCooling()
        }
    }

    companion object {

        // Default Values
        private const val DEFAULT_TORNADO_SPEED = 0.25
        private const val DEFAULT_TORNADO_COUNT = 0
        private const val SPREAD_ANGLE = 45.0  // 扩散角度

        // Parameter Names
        private const val PARAM_TORNADO_SPEED = "tornado_speed"  // 龙卷风速度
        private const val PARAM_TORNADO_COUNT = "tornado_count"  // 龙卷风数量

        // Enhancement IDs
        private const val ENHANCEMENT_SPEED = "speed"  // 对应速度
        private const val ENHANCEMENT_COUNT = "count"  // 对应数量
    }
}