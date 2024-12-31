package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

private const val REMAINING_COUNT = "RemainingEffects"

class AbsoluteDefenseSkill : Skill(
    Settings(
        id = "absolute_defense",
        types = listOf(SkillType.DEFENSE),
        cooldown = 30,
        rarity = SkillRarity.SUPERB
    )
), DamageTrigger, AutoStopTrigger, UsingRenderTrigger {

    init {
        this.settings
            .addParameter("block_sound", SoundEvents.ITEM_SHIELD_BLOCK)
            .addParameter("break_sound", SoundEvents.ITEM_SHIELD_BREAK)

        addParameter(
            name = timeParamName,
            baseValue = 30 * 20,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addParameter(
            name = "defense_count",
            baseValue = 1,
            enhancementId = "count",
            value = 1,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 4,
            descArg = Enhancement.ArgFormatters.INT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putInt(REMAINING_COUNT, getIntParam("defense_count", user, 1))
    })

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean {
        if (!player.isUsing() || amount <= 0) return false

        val data = player.getActiveData()
        val remaining = data.getInt(REMAINING_COUNT)
        if (remaining <= 0) {
            if (player.hasEnhancement("count")) {
                player.playSoundFromParam("break_sound", SoundEvents.ITEM_SHIELD_BREAK)
            } else {
                player.playSoundFromParam("block_sound", SoundEvents.ITEM_SHIELD_BLOCK)
            }
            player.stopAndCooldown()
        } else {
            player.playSoundFromParam("block_sound", SoundEvents.ITEM_SHIELD_BLOCK)
            data.putInt(REMAINING_COUNT, remaining - 1)
        }
        return true
    }
}