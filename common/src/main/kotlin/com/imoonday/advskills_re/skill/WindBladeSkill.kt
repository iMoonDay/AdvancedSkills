package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
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
    id = "wind_blade",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 10,
    rarity = SkillRarity.RARE,
    enhancements = setOf(SkillEnhancements.VELOCITY, SkillEnhancements.SUMMON_AMOUNT)
), PostAttackTrigger, PersistentTrigger, DeathTrigger, UsingRenderTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postSweepAttack(player: PlayerEntity, target: LivingEntity) {
        super.postSweepAttack(player, target)
        if (!player.isUsing() || player.world.isClient) return
        val velocity =
            player.horizontalRotationVector * (0.25 + player.getEnhancementLvl(SkillEnhancements.VELOCITY) * 0.05)
        val summonAmount = player.getEnhancementLvl(SkillEnhancements.SUMMON_AMOUNT)
        spawnTornado(player, velocity, target)
        calculateRadians(summonAmount).forEach {
            spawnTornado(player, velocity.rotateY(-it.toFloat()), target)
        }
        player.stopAndCooldown()
    }

    fun calculateRadians(n: Int): List<Double> {
        val negativeCount = floor(n / 2.0).toInt()
        val positiveCount = n - negativeCount

        val radians = mutableListOf<Double>()

        if (negativeCount > 0) {
            for (i in 0 until negativeCount) {
                val r = -(i + 1) * 45 / (negativeCount + 1)
                radians.add(r * PI / 180.0)
            }
        }

        if (positiveCount > 0) {
            for (i in 0 until positiveCount) {
                val r = (i + 1) * 45 / (positiveCount + 1)
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
}