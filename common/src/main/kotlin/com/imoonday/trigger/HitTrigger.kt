package com.imoonday.trigger

import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.item.*

interface HitTrigger : SkillTrigger {

    fun postHit(target: LivingEntity, attacker: PlayerEntity, item: ItemStack) = Unit
}