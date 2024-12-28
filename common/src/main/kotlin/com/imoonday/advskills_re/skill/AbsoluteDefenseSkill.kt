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

private const val REMAINING_EFFECTS = "RemainingEffects"

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

        addEnhanceableParameter(
            name = timeParamName,
            baseValue = 30 * 20,
            enhancementId = "time",
            value = 0.2f,
            operation = Enhancement.Operation.MULTIPLY,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
        addEnhanceableParameter(
            name = "defense_count",
            baseValue = 1,
            enhancementId = "count",
            value = 1f,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 4,
            descArg = Enhancement.ArgFormatters.INT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putInt(REMAINING_EFFECTS, user.getIntParam("defense_count"))
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
        player.playSoundFromParam("block_sound")

        val data = player.getActiveData()
        val remaining = data.getInt(REMAINING_EFFECTS)
        if (remaining <= 0) {
            if (player.hasEnhancement("count")) {
                player.playSoundFromParam("break_sound")
            }
            player.stopAndCooldown()
        } else {
            data.putInt(REMAINING_EFFECTS, remaining - 1)
        }
        return true
    }
}