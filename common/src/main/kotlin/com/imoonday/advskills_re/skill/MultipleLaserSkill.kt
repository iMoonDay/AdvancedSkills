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
//    sound = ModSounds.LASER,
//    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME, SkillEnhancements.DAMAGE, SkillEnhancements.DISTANCE)
), DangerTrigger {

    init {
        this.settings.addParameter("laser_sound", ModSounds.LASER)

        addParameter(
            name = timeParamName,
            baseValue = 10 * 20,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5
        )
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val cameraPos = player.getCameraPosVec(0f)
        val distance = getMaxDistance(player)
        val maxDistance = player.raycastVisualBlock(distance).let {
            (if (it.type == HitResult.Type.MISS) distance else it.pos.distanceTo(cameraPos))
        }
        if (usedTime % 4 == 0) {
            player.playSoundFromParam("laser_sound", ModSounds.LASER.get())
        }
        if (usedTime % 2 == 0) {
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
            val damage = getEnhancedValue(player, SkillEnhancements.DAMAGE, 2f)
            entities.forEach { it.damage(player.damageSources.magic(), damage) }
        }
    }

    override fun clientTick(player: PlayerEntity, usedTime: Int) {
        super.clientTick(player, usedTime)
        if (!player.isUsing()) return
        if (usedTime % 2 != 0) return
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

    private fun getMaxDistance(player: PlayerEntity): Double =
        64.0 * (1 + player.getEnhancementLvl(SkillEnhancements.DISTANCE) * 0.2)

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopAndCooldown(calculateCooldown(player, pressedTime))
        return UseResult.fail(Text.empty())
    }

    private fun calculateCooldown(player: PlayerEntity, pressedTime: Int) =
        (pressedTime.toFloat() / getPersistTime(player) * cooldown).toInt()

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean {
        if (player.isUsing()) player.startCooling(calculateCooldown(player, player.getUsedTime()))
        return true
    }
}