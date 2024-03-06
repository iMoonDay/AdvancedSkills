package com.imoonday.skill

import com.imoonday.component.isUsingSkill
import com.imoonday.component.startCooling
import com.imoonday.component.toggleUsingSkill
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.FluidMovementTrigger
import com.imoonday.trigger.TickTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.blockPosSet
import com.imoonday.util.translateSkill
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents

class LiquidShieldSkill : Skill(
    id = "liquid_shield",
    types = arrayOf(SkillType.DESTRUCTION),
    cooldown = 60,
    rarity = Rarity.SUPERB,
    sound = SoundEvents.BLOCK_WATER_AMBIENT
), TickTrigger, AutoStopTrigger, FluidMovementTrigger {
    override val persistTime: Int = 20 * 15
    override val skill: Skill
        get() = this

    override fun use(user: ServerPlayerEntity): UseResult {
        val active = user.toggleUsingSkill(this)
        if (!active) user.startCooling(this)
        return UseResult.consume(
            translateSkill(
                "wall_climbing", if (active) "active" else "inactive",
                name.string
            )
        )
    }

    override fun tick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsingSkill(this)) return
        val world = player.world
        val blockPos = player.blockPos
        player.boundingBox.expand(4.0)
            .blockPosSet
            .filter { it.isWithinDistance(blockPos, 4.0) && !world.getFluidState(it).isEmpty }
            .sortedByDescending { it.getSquaredDistance(blockPos) }
            .forEach { world.setBlockState(it, Blocks.AIR.defaultState) }
        super.tick(player, usedTime)
    }

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling(this)
    }

    override fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean = player.isUsingSkill(this)

    override fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double =
        if (!player.isUsingSkill(this)) speed else 0.0
}