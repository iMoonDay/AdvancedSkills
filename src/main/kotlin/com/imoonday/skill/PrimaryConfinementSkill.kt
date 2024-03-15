package com.imoonday.skill

import com.imoonday.init.ModEffects
import com.imoonday.trigger.CrosshairTrigger
import com.imoonday.trigger.UsingRenderTrigger
import com.imoonday.util.*
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import kotlin.random.Random

class PrimaryConfinementSkill : LongPressSkill(
    id = "primary_confinement",
    types = listOf(SkillType.CONTROL),
    cooldown = 12,
    rarity = Rarity.SUPERB,
    sound = SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE
), UsingRenderTrigger, CrosshairTrigger {

    override fun getMaxPressTime(): Int = 5 * 20

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        player.startCooling()
        player.swingHand(Hand.MAIN_HAND, true)
        player.raycastLivingEntity()?.takeUnless { it.type == HitResult.Type.MISS }?.let {
            if (Random.nextFloat() < 0.8f * pressedTime / getMaxPressTime()) {
                (it.entity as LivingEntity).addStatusEffect(
                    StatusEffectInstance(
                        ModEffects.CONFINEMENT,
                        20 * 3,
                        0,
                        false,
                        false,
                        true
                    )
                )
                return UseResult.success(translateSkill("primary_confinement", "success"))
            }
            return UseResult.pass(translateSkill("primary_confinement", "failed"))
        }
        return UseResult.pass(translateSkill("primary_confinement", "empty"))
    }

    private fun PlayerEntity.raycastLivingEntity(): EntityHitResult? {
        val cameraPos = getCameraPosVec(0f)
        return ProjectileUtil.raycast(
            this,
            cameraPos,
            cameraPos.add(rotationVector.multiply(5.0)),
            boundingBox.stretch(rotationVector.multiply(5.0)).expand(1.0),
            { !it.isSpectator && it.isAlive && it.isLiving },
            5.0 * 5.0
        )
    }

    override fun getCrosshair(): Crosshair {
        clientPlayer?.run {
            if (!isUsing()) return Crosshairs.NONE
            if (raycastLivingEntity()?.type == HitResult.Type.ENTITY) return Crosshairs.CROSS
        }
        return Crosshairs.NONE
    }
}