package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class DisguiseSkill : Skill(
    Settings(
        id = "disguise",
        types = listOf(SkillType.UTILITY),
        cooldown = 20,
        rarity = SkillRarity.EPIC
    )
), DisguiseTrigger, UseInterruptTrigger, AutoStopTrigger {

    init {
        settings.addParameter(
            name = PARAM_DISGUISE_DURATION,
            baseValue = DEFAULT_DISGUISE_DURATION,
            enhancementId = ENHANCEMENT_DURATION,
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this)

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_DISGUISE_DURATION, player, DEFAULT_DISGUISE_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    override fun interrupt(player: PlayerEntity) {
        player.stopAndCooldown()
    }

    override fun isDisguising(player: PlayerEntity): Boolean =
        player.isUsing() && getDisguisePos(player) != null

    fun getDisguisePos(player: PlayerEntity): BlockPos? {
        var pos = player.steppingPos
        val world = player.world
        if (world.isAir(pos) || world.getBlockState(pos).block is FluidBlock) {
            pos = pos.offset(Direction.DOWN)
        }
        if (world.isAir(pos) || world.getBlockState(pos).block is FluidBlock) {
            pos = player.blockPos.down(2)
        }
        if (world.isAir(pos) || world.getBlockState(pos).block is FluidBlock) return null
        return pos
    }

    companion object {

        // Default Values
        private const val DEFAULT_DISGUISE_DURATION = 30 * 20

        // Parameter Names
        private const val PARAM_DISGUISE_DURATION = "disguise_duration"  // 伪装时长

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应伪装时长
    }
}