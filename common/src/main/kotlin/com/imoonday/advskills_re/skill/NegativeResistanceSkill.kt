package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.trigger.renderer.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class NegativeResistanceSkill : Skill(
    id = "negative_resistance",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 30,
    rarity = Rarity.SUPERB,
), AutoStopTrigger, StatusEffectTrigger, UsingRenderTrigger {

    override val persistTime: Int = 5 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun cannotHaveStatusEffect(player: PlayerEntity, effect: StatusEffectInstance): Boolean =
        if (player.isUsing() && !effect.effectType.isBeneficial) {
            (player as? ServerPlayerEntity)?.let {
                it.playSound(ModSounds.PURIFY.get())
                it.spawnParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    false, it.centerPos, 10,
                    0.5, 0.5, 0.5, 0.1
                )
                it.stopAndCooldown()
            }
            true
        } else false

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}