package com.imoonday.trigger

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity

interface GlowingTrigger : SkillTrigger {

    fun isGlowing(player: ClientPlayerEntity, entity: Entity): Boolean = false
}