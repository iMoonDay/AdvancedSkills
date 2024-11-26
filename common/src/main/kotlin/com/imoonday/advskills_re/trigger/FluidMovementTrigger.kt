package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*
import net.minecraft.fluid.*
import net.minecraft.registry.tag.*

interface FluidMovementTrigger : SkillTrigger {

    fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double = speed

    fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean = false
}