package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*

interface SkillTrigger {

    fun getAsSkill(): Skill
    fun PlayerEntity.isUsing(): Boolean = isUsing(getAsSkill())
    fun PlayerEntity.isCooling(): Boolean = isCooling(getAsSkill())
    fun PlayerEntity.hasEquipped(): Boolean = hasEquipped(getAsSkill())
    fun PlayerEntity.getActiveData(): NbtCompound = this.getActiveData(getAsSkill()) ?: throw NO_DATA_EXCEPTION
    fun PlayerEntity.getPersistentData(): NbtCompound = this.getPersistentData(getAsSkill()) ?: throw NO_DATA_EXCEPTION
    fun PlayerEntity.clearPersistentData() = this.clearPersistentData(getAsSkill())
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

    companion object {

        private val NO_DATA_EXCEPTION = IllegalStateException("Trying to access data for an unlearned skill")
    }
}