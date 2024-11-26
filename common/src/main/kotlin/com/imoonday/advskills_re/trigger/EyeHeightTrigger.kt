package com.imoonday.advskills_re.trigger

import net.minecraft.entity.*
import net.minecraft.entity.player.*

interface EyeHeightTrigger : SkillTrigger {

    fun getEyeHeight(player: PlayerEntity, original: Float, pose: EntityPose, dimensions: EntityDimensions): Float =
        original
}