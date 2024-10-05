package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.registry.tag.TagKey

interface FluidMovementTrigger : SkillTrigger {

    fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double = speed

    fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean = false
}