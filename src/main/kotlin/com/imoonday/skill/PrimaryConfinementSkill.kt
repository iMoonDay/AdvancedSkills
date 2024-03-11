package com.imoonday.skill

import com.imoonday.init.ModEffects
import com.imoonday.trigger.UsingRenderTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.translateSkill
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import kotlin.random.Random

class PrimaryConfinementSkill : LongPressSkill(
    id = "primary_confinement",
    types = arrayOf(SkillType.CONTROL),
    cooldown = 12,
    rarity = Rarity.SUPERB,
    sound = SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE
), UsingRenderTrigger {

    override fun getMaxPressTime(): Int = 5 * 20

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        player.startCooling()
        player.swingHand(Hand.MAIN_HAND, true)
        val cameraPos = player.getCameraPosVec(0f)
        ProjectileUtil.raycast(
            player,
            cameraPos,
            cameraPos.add(player.rotationVector.multiply(5.0)),
            player.boundingBox.stretch(player.rotationVector.multiply(5.0)).expand(1.0),
            { !it.isSpectator && it.isAlive && it.isLiving },
            5.0 * 5.0
        )?.takeUnless { it.type == HitResult.Type.MISS }?.let {
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
}