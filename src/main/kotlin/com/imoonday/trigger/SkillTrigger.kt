package com.imoonday.trigger

import com.imoonday.component.*
import com.imoonday.skill.Skill
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound

interface SkillTrigger {
    fun asSkill(): Skill
    fun PlayerEntity.isUsing(): Boolean = isUsingSkill(asSkill())
    fun PlayerEntity.isCooling(): Boolean = isCooling(asSkill())
    fun PlayerEntity.startCooling() = startCooling(asSkill())
    fun PlayerEntity.startCooling(cooldown: Int) = startCooling(asSkill(), cooldown)
    fun PlayerEntity.stopCooling() = stopCooling(asSkill())
    fun PlayerEntity.modifyCooldown(operation: (Int) -> Int) = modifyCooldown(asSkill(), operation)
    fun PlayerEntity.startUsing(data: ((NbtCompound) -> Unit)? = null) =
        startUsingSkill(asSkill(), data?.let { NbtCompound().apply(it) })

    fun PlayerEntity.stopUsing() = stopUsingSkill(asSkill())
    fun PlayerEntity.toggleUsing() = toggleUsingSkill(asSkill())
}