package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.enhancement.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.text.*

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
    fun PlayerEntity.startCooling(cooldown: Int? = null) = startCooling(getAsSkill(), cooldown)
    fun PlayerEntity.stopCooling() = stopCooling(getAsSkill())
    fun PlayerEntity.modifyCooldown(operation: (Int) -> Int) = modifyCooldown(getAsSkill(), operation)
    fun PlayerEntity.startUsing(data: ((NbtCompound) -> Unit)? = null): Boolean =
        startUsing(getAsSkill(), data?.let { NbtCompound().apply(it) })

    fun PlayerEntity.stopUsing(): Boolean = stopUsing(getAsSkill())
    fun PlayerEntity.toggleUsing(): Boolean = toggleUsing(getAsSkill())
    fun PlayerEntity.isReady(): Boolean = hasEquipped() && !isCooling() && !isUsing()
    fun PlayerEntity.stopAndCooldown(cooldown: Int? = null) = stopAndCooldown(getAsSkill(), cooldown)
    fun PlayerEntity.getEnhancements(): List<SkillEnhancement> = getEnhancements(getAsSkill())
    fun <T : SkillEnhancement> PlayerEntity.getEnhancement(type: SkillEnhancementType<T>): T? =
        getEnhancement(getAsSkill(), type)

    fun PlayerEntity.getEnhancementLvl(type: SkillEnhancementType<*>): Int =
        getEnhancement(type)?.level ?: 0

    fun PlayerEntity.hasEnhancement(type: SkillEnhancementType<*>): Boolean =
        getEnhancement(type) != null

    fun <T : SkillEnhancement> PlayerEntity.addEnhancementTooltip(
        tooltip: MutableList<Text>,
        type: SkillEnhancementType<T>,
        value: (T) -> Any
    ) {
        getEnhancement(type)?.let {
            tooltip.add(getAsSkill().message(it.type.id, value(it)))
        }
    }

    companion object {

        private val NO_DATA_EXCEPTION = IllegalStateException("Trying to access data for an unlearned skill")
    }
}

inline fun <reified N : Number, T : FixedValueEnhancement> SkillTrigger.getEnhancedValue(
    player: PlayerEntity,
    type: SkillEnhancementType<T>,
    value: N
): N = player.getEnhancement(type)?.applyMultiplier(value) ?: value