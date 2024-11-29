package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.playSound
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class AbsoluteDefenseSkill : Skill(
    id = "absolute_defense",
    types = listOf(SkillType.DEFENSE),
    cooldown = 30,
    rarity = Rarity.SUPERB
), DamageTrigger, AutoStopTrigger, FeatureRendererTrigger {

    override val persistTime = 20 * 30

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean {
        if (!player.isUsing() || amount <= 0) return false
        player.playSound(SoundEvents.ITEM_SHIELD_BLOCK)
        player.stopUsing()
        return true
    }
}
