package com.imoonday.advskills_re.init

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enhancement.*
import com.imoonday.advskills_re.util.*

object SkillEnhancements {

    private val types: MutableMap<String, SkillEnhancementType<*>> = mutableMapOf()

    @JvmStatic
    val EMPTY = register("empty", 1, EmptyEnhancement.factory)

    @JvmStatic
    val COOLDOWN = register("cooldown", FixedValueEnhancement.createFactory(-0.8))

    @JvmStatic
    val PERSISTENT_TIME = register("persistent_time", FixedValueEnhancement.createFactory(1.0))

    @JvmStatic
    val CHARGE_TIME = register("charge_time", FixedValueEnhancement.createFactory(-0.8))

    @JvmStatic
    val DAMAGE = register("damage", FixedValueEnhancement.createFactory(1.0))

    @JvmStatic
    val HEALING_AMOUNT = register("healing_amount", FixedValueEnhancement.createFactory(1.0))

    @JvmStatic
    val USE_COUNT = register("use_times", ::SkillEnhancement)

    @JvmStatic
    val SUMMON_AMOUNT = register("summon_amount", ::SkillEnhancement)

    @JvmStatic
    val RANGE = register("range", ::SkillEnhancement)

    @JvmStatic
    val DISTANCE = register("distance", ::SkillEnhancement)

    @JvmStatic
    val EFFECT_COUNT = register("effect_count", ::SkillEnhancement)

    @JvmStatic
    val DEFENSE_EFFECT = register("defense_effect", ::SkillEnhancement)

    @JvmStatic
    val CHARGE_SLOWDOWN = register("charge_slowdown", FixedValueEnhancement.createFactory(-1.0))

    @JvmStatic
    val TIME_UP_LIMIT = register("time_up_limit", FixedValueEnhancement.createFactory(1.0))

    @JvmStatic
    val MOVEMENT_SPEED = register("movement_speed", ::SkillEnhancement)

    @JvmStatic
    val LAUNCH_COUNT = register("launch_count", ::SkillEnhancement)

    @JvmStatic
    val EFFECT_VALUE = register("effect_value", ::SkillEnhancement)

    @JvmStatic
    val STATUS_EFFECT_DURATION = register("status_effect_duration", FixedValueEnhancement.createFactory(1.0))

    @JvmStatic
    val STATUS_EFFECT_AMPLIFIER = register("status_effect_amplifier", ::SkillEnhancement)

    @JvmStatic
    val VELOCITY = register("velocity", ::SkillEnhancement)

    @JvmStatic
    val POWER = register("power", ::SkillEnhancement)

    @JvmStatic
    val CHANCE = register("chance", ::SkillEnhancement)

    @JvmStatic
    val USE_COST = register("use_cost", ::SkillEnhancement)

    @JvmStatic
    val EFFECT_FREQUENCY = register("effect_frequency", ::SkillEnhancement)

    fun init() = Unit

    @JvmStatic
    fun <T : SkillEnhancement> register(type: SkillEnhancementType<T>): SkillEnhancementType<T> {
        if (types.containsKey(type.id)) {
            throw IllegalArgumentException("Duplicate skill enhancement type id: ${type.id}")
        }
        types[type.id] = type
        return type
    }

    private fun <T : SkillEnhancement> register(
        id: String,
        maxLevel: Int,
        factory: SkillEnhancementType.Factory<T>,
    ): SkillEnhancementType<T> = register(
        SkillEnhancementType(
            id,
            translate("skillEnhancement.$id.name"),
            translate("skillEnhancement.$id.typeDescription"),
            maxLevel,
            factory
        )
    )

    private fun <T : SkillEnhancement> register(
        name: String,
        factory: SkillEnhancementType.Factory<T>,
    ): SkillEnhancementType<T> = register(name, 5, factory)

    @JvmStatic
    fun get(id: String): SkillEnhancementType<*>? = types[id]

    @JvmStatic
    fun getValues(): List<SkillEnhancementType<*>> = types.values.toList()
}