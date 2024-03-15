package com.imoonday.trigger

import com.imoonday.skill.Skill
import com.imoonday.util.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound

interface SkillTrigger {

    fun getAsSkill(): Skill
    fun PlayerEntity.isUsing(): Boolean = isUsing(getAsSkill())

    fun PlayerEntity.isCooling(): Boolean = isCooling(getAsSkill())
    fun PlayerEntity.hasEquipped(): Boolean = hasEquipped(getAsSkill())
    fun PlayerEntity.getUsingData(): NbtCompound? = getUsingData(getAsSkill())

    fun PlayerEntity.getUsedTime(): Int = getUsedTime(getAsSkill())
    fun PlayerEntity.modifyUsedTime(operation: (Int) -> Int) = modifyUsedTime(getAsSkill(), operation)

    fun PlayerEntity.startCooling() = startCooling(getAsSkill())
    fun PlayerEntity.startCooling(cooldown: Int) = startCooling(getAsSkill(), cooldown)
    fun PlayerEntity.stopCooling() = stopCooling(getAsSkill())
    fun PlayerEntity.modifyCooldown(operation: (Int) -> Int) = modifyCooldown(getAsSkill(), operation)
    fun PlayerEntity.startUsing(data: ((NbtCompound) -> Unit)? = null): Boolean =
        startUsing(getAsSkill(), data?.let { NbtCompound().apply(it) })

    fun PlayerEntity.stopUsing(): Boolean = stopUsing(getAsSkill())
    fun PlayerEntity.toggleUsing(): Boolean = toggleUsing(getAsSkill())
    fun PlayerEntity.isReady(): Boolean = hasEquipped() && !isCooling() && !isUsing()
}