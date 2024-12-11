package com.imoonday.advskills_re.skill.enhancement

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.text.*

open class SkillEnhancement(
    val type: SkillEnhancementType<*>,
    var level: Int
) {

    val name: Text = type.name
    open val description: Text = createDescription()

    fun save(nbt: NbtCompound = NbtCompound()): NbtCompound {
        nbt.putString("id", type.id)
        return saveWithoutId(nbt)
    }

    fun saveWithoutId(nbt: NbtCompound = NbtCompound()): NbtCompound {
        nbt.putInt("level", level)
        return saveAdditionalData(nbt)
    }

    open fun saveAdditionalData(nbt: NbtCompound = NbtCompound()): NbtCompound = nbt

    fun load(nbt: NbtCompound) {
        if (nbt.contains("level")) {
            level = nbt.getInt("level")
        }
        loadAdditionalData(nbt)
    }

    open fun loadAdditionalData(nbt: NbtCompound) = Unit

    fun copy(): SkillEnhancement =
        type.create(level).apply { loadAdditionalData(this@SkillEnhancement.saveAdditionalData()) }

    fun copyWithLevel(level: Int): SkillEnhancement =
        type.create(level).apply { loadAdditionalData(this@SkillEnhancement.saveAdditionalData()) }

    fun createDescription(vararg args: Any): MutableText =
        translate("skillEnhancement.${type.id}.description", *args)

    fun createDescriptionWithSuffix(suffix: String, vararg args: Any): MutableText =
        translate("skillEnhancement.${type.id}.description.$suffix", *args)

    fun isSuitableFor(skill: Skill): Boolean = !this.isEmpty() && !skill.isEmpty() && skill.isAvailable(this)

    fun isSuitableFor(player: PlayerEntity, skill: Skill): Boolean =
        !this.isEmpty() && !skill.isEmpty() && skill.isAvailableFor(player, this)

    fun isOf(type: SkillEnhancementType<*>): Boolean = this.type == type

    fun isSameType(other: SkillEnhancement): Boolean = this.type == other.type

    fun isEmpty(): Boolean = this.type == SkillEnhancements.EMPTY || this is EmptyEnhancement || this.level <= 0

    fun isMaxLevel(): Boolean = level >= type.maxLevel

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