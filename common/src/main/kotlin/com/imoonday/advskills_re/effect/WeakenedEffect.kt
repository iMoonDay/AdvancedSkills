package com.imoonday.advskills_re.effect

import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.nbt.*

class WeakenedEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    4738376
), SyncClientEffect {

    override val syncId: String = "weakened"

    override fun writeData(entity: LivingEntity, nbt: NbtCompound): NbtCompound = nbt.apply {
        putInt("level", entity.getStatusEffect(this@WeakenedEffect)?.amplifier?.plus(1) ?: 0)
    }
}