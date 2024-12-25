package com.imoonday.advskills_re.skill.enhancement

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import net.minecraft.nbt.*
import net.minecraft.text.*

open class SkillEnhancement(
    val type: SkillEnhancementType<*>,
    var level: Int
) {

    val name: Text = type.name
    open val description: Text = createDescription()

    fun load(nbt: NbtCompound) {
        if (nbt.contains("level")) {
            level = nbt.getInt("level")
        }
        loadAdditionalData(nbt)
    }

    open fun loadAdditionalData(nbt: NbtCompound) = Unit

    fun createDescription(vararg args: Any): MutableText =
        translate("skillEnhancement.${type.id}.description", *args)

    fun isOf(type: SkillEnhancementType<*>): Boolean = this.type == type

    fun isEmpty(): Boolean = this.type == SkillEnhancements.EMPTY || this is EmptyEnhancement || this.level <= 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SkillEnhancement) return false

        if (type != other.type) return false
        if (level != other.level) return false
        if (name != other.name) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + level
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }

    override fun toString(): String = "SkillEnhancement(type=$type, name=$name, level=$level)"
}