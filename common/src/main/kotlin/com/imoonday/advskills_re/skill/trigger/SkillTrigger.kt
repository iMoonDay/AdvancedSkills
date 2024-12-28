package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.enhancement.*
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
    fun PlayerEntity.getPersistentData(): NbtCompound = this.getPersistentData(getAsSkill()) ?: throw NO_DATA_EXCEPTION()
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

    @Deprecated("Use getEnhancement(id) instead", ReplaceWith("TODO()"))
    fun <T : SkillEnhancement> PlayerEntity.getEnhancement(type: SkillEnhancementType<T>): T? =
        type.factory.create(type, 0)

    @Deprecated("Use getParameter(name) instead", ReplaceWith("TODO()"))
    fun PlayerEntity.getEnhancementLvl(type: SkillEnhancementType<*>): Int =
        getEnhancement(type)?.level ?: 0

    @Deprecated("Use hasEnhancement(id) instead", ReplaceWith("TODO()"))
    fun PlayerEntity.hasEnhancement(type: SkillEnhancementType<*>): Boolean =
        getEnhancement(type) != null

    fun PlayerEntity.getEnhancement(id: String): Pair<Enhancement, Int>? =
        getEnhancement(getAsSkill(), id)

    fun PlayerEntity.getEnhancementValue(id: String): Float {
        val pair = getEnhancement(getAsSkill(), id) ?: return 0.0f
        return pair.first.getValue(pair.second)
    }

    fun PlayerEntity.getDoubleParam(
        name: String,
        default: Double? = null,
        min: Double = Double.MIN_VALUE,
        max: Double = Double.MAX_VALUE
    ): Double = getFloatParam(name, default?.toFloat(), min.toFloat(), max.toFloat()).toDouble()

    fun PlayerEntity.getFloatParam(
        name: String,
        default: Float? = null,
        min: Float = Float.MIN_VALUE,
        max: Float = Float.MAX_VALUE
    ): Float {
        val skill = getAsSkill()
        val parameter = skill.getParam(name)?.asFloat() ?: return default ?: 0f
        val baseValue = parameter.baseValue
        var result = baseValue
        for (id in parameter.enhancements) {
            val pair = getEnhancement(skill, id) ?: continue
            result = pair.first.getEnhancedValue(pair.second, result)
        }
        return result.coerceIn(min, max)
    }

    fun PlayerEntity.getIntParam(
        name: String,
        default: Int? = null,
        min: Int = Int.MIN_VALUE,
        max: Int = Int.MAX_VALUE
    ): Int {
        val skill = getAsSkill()
        val parameter = skill.getParam(name)?.asInt() ?: return default ?: 0
        val baseValue = parameter.baseValue
        var result = baseValue
        for (id in parameter.enhancements) {
            val pair = getEnhancement(skill, id) ?: continue
            result = pair.first.getEnhancedValue(pair.second, result)
        }
        return result.coerceIn(min, max)
    }

    fun PlayerEntity.getBooleanParam(name: String, default: Boolean? = null): Boolean {
        val skill = getAsSkill()
        val parameter = skill.getParam(name)?.asBoolean() ?: return default ?: false
        val baseValue = parameter.baseValue
        return if (parameter.enhancements.any { hasEnhancement(it) }) !baseValue else baseValue
    }

    fun getStringParam(name: String, default: String? = null): String {
        val skill = getAsSkill()
        val parameter =
            skill.getParam(name)?.asString() ?: return default ?: ""
        return parameter.baseValue
    }

    fun getIdentifierParam(name: String): Identifier? = try {
        getStringParam(name).toIdentifier()
    } catch (e: IllegalArgumentException) {
        null
    }

    fun getSoundEventParam(name: String): SoundEvent? {
        val skill = getAsSkill()
        val parameter = skill.getParam(name)?.asSoundEvent() ?: return null
        return parameter.baseValue.getOrNull()
    }

    fun getListParam(name: String): List<SkillParameter> {
        val skill = getAsSkill()
        val parameter = skill.getParam(name)?.asList() ?: return emptyList()
        return parameter.baseValue
    }

    fun getStringListParam(name: String): List<String> = getListParam(name).map { it.asString().baseValue }

    fun getParamBaseValue(name: String, default: Any? = null): Any {
        val skill = getAsSkill()
        val parameter =
            skill.getParam(name) ?: return default ?: throw NoParameterException(skill, name)
        return parameter.baseValue
    }

    fun PlayerEntity.hasEnhancement(id: String): Boolean =
        getEnhancement(getAsSkill(), id) != null

    companion object {

        private val NO_DATA_EXCEPTION = { IllegalStateException("Trying to access data for an unlearned skill") }
    }
}

@Deprecated("Use getParam instead", ReplaceWith("TODO()"))
inline fun <reified N : Number> SkillTrigger.getEnhancedValue(
    player: PlayerEntity,
    type: SkillEnhancementType<FixedValueEnhancement>,
    value: N
): N = player.getEnhancement(type)?.applyMultiplier(value) ?: value