package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.*
import net.minecraft.util.hit.*
import kotlin.random.*

class PrimaryConfinementSkill : LongPressSkill(
    id = "primary_confinement",
    types = listOf(SkillType.CONTROL),
    cooldown = 12,
    rarity = Rarity.SUPERB,
    sound = SoundEvents::BLOCK_ENCHANTMENT_TABLE_USE
), UsingRenderTrigger, CrosshairTrigger, TargetRenderTrigger {

    override fun getMaxPressTime(): Int = 5 * 20

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        player.startCooling()
        player.swingHand(Hand.MAIN_HAND, true)
        player.raycastLivingEntity(5.0)?.takeIf { it.type == HitResult.Type.ENTITY }?.let {
            if (Random.nextFloat() < 0.8f * pressedTime / getMaxPressTime()) {
                (it.entity as LivingEntity).addStatusEffect(
                    StatusEffectInstance(
                        ModEffects.CONFINEMENT.get(),
                        20 * 3,
                        0,
                        false,
                        false,
                        true
                    )
                )
                return UseResult.success(message("success"))
            }
            return UseResult.pass(message("failed"))
        }
        return UseResult.pass(message( "empty"))
    }

    override fun getCrosshair(): Crosshair {
        clientPlayer?.run {
            if (!isUsing()) return Crosshairs.NONE
            if (raycastLivingEntity(5.0)?.type == HitResult.Type.ENTITY) return Crosshairs.CROSS
        }
        return Crosshairs.NONE
    }

    override fun isTarget(player: PlayerEntity, entity: LivingEntity): Boolean {
        if (!player.isUsing()) return false
        return player.raycastLivingEntity(5.0)?.entity == entity
    }
}