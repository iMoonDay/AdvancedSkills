package com.imoonday.trigger

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

interface HitTrigger {

    fun postHit(target: LivingEntity, attacker: PlayerEntity, item: ItemStack)
}