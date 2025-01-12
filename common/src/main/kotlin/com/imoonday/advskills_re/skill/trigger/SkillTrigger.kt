package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.sound.*
import net.minecraft.util.*
import kotlin.Pair
import kotlin.jvm.optionals.*

interface SkillTrigger {

    fun getAsSkill(): Skill
    fun PlayerEntity.isUsing(): Boolean = isUsing(getAsSkill())
    fun PlayerEntity.isCooling(): Boolean = isCooling(getAsSkill())
    fun PlayerEntity.hasEquipped(): Boolean = hasEquipped(getAsSkill())
    fun PlayerEntity.getActiveData(): NbtCompound = this.getActiveData(getAsSkill()) ?: throw NO_DATA_EXCEPTION()
    fun PlayerEntity.getPersistentData(): NbtCompound =
        this.getPersistentData(getAsSkill()) ?: throw NO_DATA_EXCEPTION()

    fun PlayerEntity.clearPersistentData() = this.clearPersistentData(getAsSkill())
    fun PlayerEntity.getUsedTime(): Int = getUsedTime(getAsSkill())
    fun PlayerEntity.modifyUsedTime(operation: (Int) -> Int) = modifyUsedTime(getAsSkill(), operation)
    fun PlayerEntity.startCooling(cooldown: Int? = null) = startCooling(getAsSkill(), cooldown)
    fun PlayerEntity.stopCooling() = stopCooling(getAsSkill())
    fun PlayerEntity.modifyCooldown(operation: (Int) -> Int) = modifyCooldown(getAsSkill(), operation)
    fun PlayerEntity.startUsing(data: ((NbtCompound) -> Unit)? = null): Boolean =
        startUsing(getAsSkill(), data?.let { NbtCompound().apply(it) })

    fun PlayerEntity.stopUsing(): Boolean = stopUsing(getAsSkill())
    fun PlayerEntity.toggleUsing(data: NbtCompound? = null): Boolean = toggleUsing(getAsSkill(), data)
    fun PlayerEntity.isReady(): Boolean = hasEquipped() && !isCooling() && !isUsing()
    fun PlayerEntity.stopAndCooldown(cooldown: Int? = null) = stopAndCooldown(getAsSkill(), cooldown)
    fun PlayerEntity.getEnhancements(): Map<Enhancement, EnhancementData> = getEnhancements(getAsSkill())

    fun PlayerEntity.getEnhancement(id: String): Pair<Enhancement, EnhancementData>? =
        getEnhancement(getAsSkill(), id)

    fun PlayerEntity.getEnhancementValue(id: String): Double {
        val pair = getEnhancement(getAsSkill(), id) ?: return 0.0
        return pair.first.getValue(pair.second.currentLevel)
    }

    fun getDoubleParam(
        name: String,
        player: PlayerEntity?,
        default: Double?,
        min: Double = Double.MIN_VALUE,
        max: Double = Double.MAX_VALUE
    ): Double {
        val skill = getAsSkill()
        val parameter = skill.getParam(name)?.asDouble() ?: return default ?: 0.0
        val baseValue = parameter.baseValue

        if (player == null) return baseValue
        return Enhancement.calculateValue<Double>(player.getEnhancements(skill, parameter), baseValue)
            .coerceIn(min, max)
    }

    fun getFloatParam(
        name: String,
        player: PlayerEntity?,
        default: Float?,
        min: Float = Float.MIN_VALUE,
        max: Float = Float.MAX_VALUE
    ): Float {
        val skill = getAsSkill()
        val parameter = skill.getParam(name)?.asFloat() ?: return default ?: 0f
        val baseValue = parameter.baseValue

        if (player == null) return baseValue
        return Enhancement.calculateValue<Float>(player.getEnhancements(skill, parameter), baseValue).coerceIn(min, max)
    }

    fun getIntParam(
        name: String,
        player: PlayerEntity?,
        default: Int?,
        min: Int = Int.MIN_VALUE,
        max: Int = Int.MAX_VALUE
    ): Int {
        val skill = getAsSkill()
        val param = skill.getParam(name)
        val parameter = param?.asInt() ?: return default ?: 0
        val baseValue = parameter.baseValue

        if (player == null) return baseValue
        return Enhancement.calculateValue<Int>(player.getEnhancements(skill, parameter), baseValue).coerceIn(min, max)
    }

    fun getBooleanParam(name: String, player: PlayerEntity?, default: Boolean?): Boolean {
        val skill = getAsSkill()
        val parameter = skill.getParam(name)?.asBoolean() ?: return default ?: false
        val baseValue = parameter.baseValue
        return if (player != null && parameter.enhancements.any {
                player.getEnhancement(it)?.second?.activated == true
            }) !baseValue else baseValue
    }

    fun getStringParam(name: String, default: String?): String {
        val skill = getAsSkill()
        val parameter = skill.getParam(name)?.asString() ?: return default ?: ""
        return parameter.baseValue
    }

    fun getIdentifierParam(name: String, default: Identifier?): Identifier? = try {
        getStringParam(name, "").toIdentifier() ?: default
    } catch (e: IllegalArgumentException) {
        null
    }

    fun getSoundEventParam(name: String, default: SoundEvent?): SoundEvent? {
        val skill = getAsSkill()
        val parameter = skill.getParam(name)?.asSoundEvent() ?: return default
        return parameter.baseValue.getOrDefault(default)
    }

    fun getListParam(name: String): Parameter.ListParameter =
        getAsSkill().getParam(name)?.asList() ?: Parameter.ListParameter(listOf(), listOf())

    fun PlayerEntity.hasEnhancement(id: String): Boolean =
        getEnhancement(getAsSkill(), id) != null

    companion object {

        private val NO_DATA_EXCEPTION = { IllegalStateException("Error! Trying to access data for an unlearned skill") }
    }
}

inline fun <reified T : Any> SkillTrigger.getParamBaseValue(name: String, default: T? = null): T? {
    val skill = getAsSkill()
    val parameter = skill.getParam(name) ?: return default
    val baseValue = parameter.baseValue
    return baseValue as? T ?: default
}