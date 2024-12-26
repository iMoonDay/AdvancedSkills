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
    fun PlayerEntity.getEnhancements(): Map<Enhancement, Int> = getEnhancements(getAsSkill())
    fun <T : SkillEnhancement> PlayerEntity.getEnhancement(type: SkillEnhancementType<T>): T? = TODO()

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

    fun PlayerEntity.getEnhancement(id: String): Pair<Enhancement, Int>? =
        getEnhancement(getAsSkill(), id)

    fun PlayerEntity.getEnhancementValue(id: String): Float {
        val pair = getEnhancement(getAsSkill(), id) ?: return 0.0f
        return pair.first.getValue(pair.second)
    }

    fun PlayerEntity.getDoubleParameter(
        name: String,
        default: Double? = null,
        min: Double? = null,
        max: Double? = null
    ): Double = getFloatParameter(name, default?.toFloat(), min?.toFloat(), max?.toFloat()).toDouble()

    fun PlayerEntity.getFloatParameter(
        name: String,
        default: Float? = null,
        min: Float? = null,
        max: Float? = null
    ): Float {
        val skill = getAsSkill()
        val parameter = skill.getFloatParameter(name) ?: return default
            ?: throw IllegalArgumentException("No such parameter: $name")
        val baseValue = parameter.baseValue
        val enhancementId = parameter.enhancement ?: return baseValue
        val pair = getEnhancement(skill, enhancementId) ?: return baseValue
        var value = pair.first.getEnhancedValue(pair.second, baseValue)
        if (min != null) value = maxOf(value, min)
        if (max != null) value = minOf(value, max)
        return value
    }

    fun PlayerEntity.getIntParameter(name: String, default: Int? = null, min: Int? = null, max: Int? = null): Int {
        val skill = getAsSkill()
        val parameter =
            skill.getIntParameter(name) ?: return default ?: throw IllegalArgumentException("No such parameter: $name")
        val baseValue = parameter.baseValue
        val enhancementId = parameter.enhancement ?: return baseValue
        val pair = getEnhancement(skill, enhancementId) ?: return baseValue
        var value = pair.first.getEnhancedValue(pair.second, baseValue)
        if (min != null) value = maxOf(value, min)
        if (max != null) value = minOf(value, max)
        return value
    }

    fun PlayerEntity.getStringParameter(name: String, default: String? = null): String {
        val skill = getAsSkill()
        val parameter =
            skill.getStringParameter(name) ?: return default
                ?: throw IllegalArgumentException("No such parameter: $name")
        return parameter.baseValue
    }

    fun getParameterBaseValue(name: String, default: Any? = null): Any {
        val skill = getAsSkill()
        val parameter =
            skill.getParameter(name) ?: return default ?: throw IllegalArgumentException("No such parameter: $name")
        return parameter.baseValue
    }

    fun PlayerEntity.hasEnhancement(id: String): Boolean =
        getEnhancement(getAsSkill(), id) != null

    companion object {

        private val NO_DATA_EXCEPTION = IllegalStateException("Trying to access data for an unlearned skill")
    }
}

inline fun <reified N : Number> SkillTrigger.getEnhancedValue(
    player: PlayerEntity,
    type: SkillEnhancementType<FixedValueEnhancement>,
    value: N
): N = player.getEnhancement(type)?.applyMultiplier(value) ?: value