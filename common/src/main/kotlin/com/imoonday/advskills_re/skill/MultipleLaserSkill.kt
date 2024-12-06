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
    id = "multiple_laser",
    types = listOf(SkillType.ATTACK),
    cooldown = 45,
    rarity = SkillRarity.LEGENDARY,
    sound = ModSounds.LASER
), DangerTrigger {

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val cameraPos = player.getCameraPosVec(0f)
        val maxDistance = player.raycastVisualBlock(64.0).let {
            (if (it.type == HitResult.Type.MISS) 64.0 else it.pos.distanceTo(cameraPos))
        }
        if (usedTime % 4 == 0) {
            player.playSkillSound()
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
            entities.forEach { it.damage(player.damageSources.magic(), 2f) }
        }
    }

    override fun clientTick(player: PlayerEntity, usedTime: Int) {
        super.clientTick(player, usedTime)
        if (!player.isUsing()) return
        if (usedTime % 2 != 0) return
        val start = player.centerPos
        val length = player.raycastVisualBlock(64.0).pos.distanceTo(start)
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

    override fun getMaxPressTime(): Int = 10 * 20

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopAndCooldown(calculateCooldown(pressedTime))
        return UseResult.fail(Text.empty())
    }

    private fun calculateCooldown(pressedTime: Int) =
        (pressedTime.toFloat() / getMaxPressTime() * cooldown).toInt()

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean {
        if (player.isUsing()) player.startCooling(calculateCooldown(player.getUsedTime()))
        return true
    }
}