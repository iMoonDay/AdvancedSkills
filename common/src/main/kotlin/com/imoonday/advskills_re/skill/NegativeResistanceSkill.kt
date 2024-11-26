package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.playSound
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class NegativeResistanceSkill : Skill(
    id = "negative_resistance",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 30,
    rarity = Rarity.SUPERB,
), AutoStopTrigger, StatusEffectTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override val persistTime: Int = 20 * 5

    override fun cannotHaveStatusEffect(player: PlayerEntity, effect: StatusEffectInstance): Boolean =
        if (player.isUsing() && !effect.effectType.isBeneficial) {
            (player as? ServerPlayerEntity)?.let {
                it.playSound(ModSounds.PURIFY.get())
                it.stopUsing()
            }
            true
        } else false
}