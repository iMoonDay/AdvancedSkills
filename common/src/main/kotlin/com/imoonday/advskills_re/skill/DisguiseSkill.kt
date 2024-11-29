package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class DisguiseSkill : Skill(
    id = "disguise",
    types = listOf(SkillType.FUNCTION),
    cooldown = 20,
    rarity = Rarity.EPIC
), DisguiseTrigger, UseInterruptTrigger, AutoStopTrigger {

    override val persistTime: Int = 20 * 30

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    override fun interrupt(player: PlayerEntity) {
        player.stopUsing()
        player.startCooling()
    }

    override fun isDisguising(player: PlayerEntity): Boolean = player.isUsing() && getDisguisePos(player) != null

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
}