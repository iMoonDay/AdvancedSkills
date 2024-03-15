package com.imoonday.trigger

import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.player.PlayerEntity

interface EyeHeightTrigger : SkillTrigger {

    fun getEyeHeight(player: PlayerEntity, original: Float, pose: EntityPose, dimensions: EntityDimensions): Float =
        original
}