package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.entity.projectile.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.text.*
import net.minecraft.util.hit.*
import org.joml.*

class MultipleLaserSkill : LongPressSkill(
    Settings(
        id = "multiple_laser",
        types = listOf(SkillType.ATTACK),
        cooldown = 45,
        rarity = SkillRarity.LEGENDARY
    )
), DangerTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_DAMAGE_INTERVAL, DEFAULT_DAMAGE_INTERVAL)
            .addParameter(PARAM_LASER_SOUND, DEFAULT_LASER_SOUND)
            .addParameter(
                name = PARAM_LASER_DURATION,
                baseValue = DEFAULT_LASER_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            ).addParameter(
                name = PARAM_LASER_DAMAGE,
                baseValue = DEFAULT_LASER_DAMAGE,
                enhancementId = ENHANCEMENT_DAMAGE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_LASER_RANGE,
                baseValue = DEFAULT_LASER_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val cameraPos = player.getCameraPosVec(0f)
        val range = getMaxDistance(player)
        val maxDistance = player.raycastVisualBlock(range).let {
            (if (it.type == HitResult.Type.MISS) range else it.pos.distanceTo(cameraPos))
        }
        if (usedTime % 4 == 0) {
            player.playSoundFromParam(PARAM_LASER_SOUND, DEFAULT_LASER_SOUND.get())
        }
        val interval = getIntParam(PARAM_DAMAGE_INTERVAL, player, DEFAULT_DAMAGE_INTERVAL, 1)
        if (usedTime % interval == 0) {
            val entities: MutableList<LivingEntity> = mutableListOf()
            while (true) {
                ProjectileUtil.raycast(
                    player,
                    cameraPos,
                    cameraPos.add(player.rotationVector.multiply(maxDistance)),
                    player.boundingBox.stretch(player.rotationVector.multiply(maxDistance)),
                    { !it.isSpectator && it.isAlive && it.isLiving && it !in entities },
                    maxDistance * maxDistance
                )?.takeUnless { it.type == HitResult.Type.MISS }?.let {
                    entities.add(it.entity as LivingEntity)
                } ?: break
            }
            val damage = getFloatParam(PARAM_LASER_DAMAGE, player, DEFAULT_LASER_DAMAGE)
            entities.forEach { it.damage(player.damageSources.magic(), damage) }
        }
    }

    override fun clientTick(player: PlayerEntity, usedTime: Int) {
        super.clientTick(player, usedTime)
        if (!player.isUsing()) return
        val interval = getIntParam(PARAM_DAMAGE_INTERVAL, player, DEFAULT_DAMAGE_INTERVAL, 1)
        if (usedTime % interval != 0) return
        val start = player.centerPos
        val length = player.raycastVisualBlock(getMaxDistance(player)).pos.distanceTo(start)
        val color = Vector3f(0f, 1f, 0f)
        var offset = 0.1
        val world = player.world
        while (offset <= length) {
            val pos = start + player.rotationVector * offset
            world.addParticle(
                DustParticleEffect(color, 1f),
                true,
                pos.x, pos.y, pos.z,
                0.0, 0.0, 0.0,
            )
            offset += 0.1
        }
    }

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_LASER_DURATION, player, DEFAULT_LASER_DURATION, 0)

    private fun getMaxDistance(player: PlayerEntity): Double =
        getDoubleParam(PARAM_LASER_RANGE, player, DEFAULT_LASER_RANGE)

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopAndCooldown(calculateCooldown(player, pressedTime))
        return UseResult.fail(Text.empty())
    }

    private fun calculateCooldown(player: PlayerEntity, pressedTime: Int) =
        (pressedTime.toFloat() / getMaxUseTime(player) * cooldown).toInt()

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean {
        if (player.isUsing()) player.startCooling(calculateCooldown(player, player.getUsedTime()))
        return true
    }

    companion object {
        // Default Values
        private const val DEFAULT_DAMAGE_INTERVAL = 2
        private const val DEFAULT_LASER_DURATION = 10 * 20
        private const val DEFAULT_LASER_DAMAGE = 2.0f
        private const val DEFAULT_LASER_RANGE = 64.0
        private val DEFAULT_LASER_SOUND = ModSounds.LASER

        // Parameter Names
        private const val PARAM_DAMAGE_INTERVAL = "damage_interval"  // 伤害间隔
        private const val PARAM_LASER_SOUND = "laser_sound"  // 激光音效
        private const val PARAM_LASER_DURATION = "laser_duration"  // 激光持续时间
        private const val PARAM_LASER_DAMAGE = "laser_damage"  // 激光伤害
        private const val PARAM_LASER_RANGE = "laser_range"  // 激光射程

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_DAMAGE = "damage"  // 对应伤害
        private const val ENHANCEMENT_RANGE = "range"  // 对应射程
    }
}