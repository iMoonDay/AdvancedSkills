package com.imoonday.util

import com.imoonday.skill.Skill
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text

sealed class SkillSlot(
    val index: Int,
    skill: Skill = Skill.EMPTY,
) {

    var skill = skill
        private set
    abstract val type: String
    open val u: Int = (index - 1) * 9
    abstract val v: Int
    val tooltip: Text
        get() = translate("skillSlot", type, index)

    fun isEmpty() = skill.isEmpty()
    fun toNbt(): NbtCompound = NbtCompound().apply {
        putInt("index", index)
        putString("type", this@SkillSlot.type)
        putString("skill", skill.id.toString())
    }

    abstract fun copyWithIndex(index: Int): SkillSlot
    abstract fun canEquip(skill: Skill): Boolean
    fun equip(skill: Skill, callback: (Boolean) -> Unit = {}): Boolean =
        if (this.skill != skill && canEquip(skill)) {
            this.skill = skill
            callback.invoke(true)
            true
        } else {
            callback.invoke(false)
            false
        }

    fun unequip(callback: (Boolean) -> Unit = {}): Boolean =
        if (skill.isEmpty()) {
            callback.invoke(false)
            false
        } else {
            skill = Skill.EMPTY
            callback.invoke(true)
            true
        }

    fun unequipIf(predicate: (Skill) -> Boolean, callback: (Boolean) -> Unit = {}): Boolean =
        if (predicate.invoke(skill)) {
            unequip(callback)
        } else false

    class Generic(index: Int, skill: Skill = Skill.EMPTY) : SkillSlot(index, skill) {

        override val type: String = "generic"
        override val v: Int = 0

        override fun canEquip(skill: Skill): Boolean = true

        override fun copyWithIndex(index: Int): Generic = Generic(index, skill)
    }

    class Active(index: Int, skill: Skill = Skill.EMPTY) : SkillSlot(index, skill) {

        override val type: String = "active"
        override val v: Int = 9
        override fun canEquip(skill: Skill): Boolean =
            SkillType.PASSIVE !in skill.types || skill.isEmpty()

        override fun copyWithIndex(index: Int): Active = Active(index, skill)
    }

    class Passive(index: Int, skill: Skill = Skill.EMPTY) : SkillSlot(index, skill) {

        override val type: String = "passive"
        override val v: Int = 9 * 2

        override fun canEquip(skill: Skill): Boolean =
            SkillType.PASSIVE in skill.types || skill.isEmpty()

        override fun copyWithIndex(index: Int): Passive = Passive(index, skill)
    }

    companion object {

        val indexTexture = id("index.png")

        fun fromNbt(tag: NbtCompound): SkillSlot {
            val index = tag.getInt("index")
            val type = tag.getString("type")
            val skill = Skill.fromId(tag.getString("skill"))
            return when (type) {
                "active" -> Active(index, skill)
                "passive" -> Passive(index, skill)
                else -> Generic(index, skill)
            }
        }

        fun isValidIndex(player: PlayerEntity, index: Int): Boolean = player.skillContainer.getSlot(index) != null
    }
}