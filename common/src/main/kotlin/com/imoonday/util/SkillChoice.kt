package com.imoonday.util

import com.imoonday.skill.Skill
import net.minecraft.nbt.NbtCompound

data class SkillChoice(
    val first: Skill,
    val second: Skill,
    val third: Skill,
) {

    fun isEmpty() = first.invalid && second.invalid && third.invalid

    fun hasEmpty() = first.invalid || second.invalid || third.invalid

    val skills = listOf(first, second, third)

    fun withFirst(skill: Skill): SkillChoice = SkillChoice(skill, second, third)

    fun withSecond(skill: Skill): SkillChoice = SkillChoice(first, skill, third)

    fun withThird(skill: Skill): SkillChoice = SkillChoice(first, second, skill)

    fun replaceWith(predicate: (Skill) -> Boolean, generator: (Set<Skill>) -> Skill): SkillChoice {
        var choice = this
        if (predicate(first)) choice = choice.withFirst(generator(choice.getNoEmpty(first)))
        if (predicate(second)) choice = choice.withSecond(generator(choice.getNoEmpty(second)))
        if (predicate(third)) choice = choice.withThird(generator(choice.getNoEmpty(third)))
        return choice
    }

    fun getNoEmpty(except: Skill? = null) = skills.filterNot { it == except || it.invalid }.toSet()

    fun toNbt(): NbtCompound = NbtCompound().apply {
        putString("1", first.id.toString())
        putString("2", second.id.toString())
        putString("3", third.id.toString())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SkillChoice) return false

        if (first != other.first) return false
        if (second != other.second) return false
        if (third != other.third) return false

        return true
    }

    override fun hashCode(): Int {
        var result = first.hashCode()
        result = 31 * result + second.hashCode()
        result = 31 * result + third.hashCode()
        return result
    }

    companion object {

        @JvmField
        val EMPTY = SkillChoice(Skill.EMPTY, Skill.EMPTY, Skill.EMPTY)

        fun fromNbt(nbt: NbtCompound): SkillChoice = SkillChoice(
            Skill.fromId(nbt.getString("1")),
            Skill.fromId(nbt.getString("2")),
            Skill.fromId(nbt.getString("3")),
        )

        fun canGenerate(except: Collection<Skill> = emptyList(), filter: (Skill) -> Boolean = { true }) =
            Skill.getLearnableSkills(except, filter).isNotEmpty()

        fun generate(except: Collection<Skill> = emptyList(), filter: (Skill) -> Boolean = { true }): SkillChoice =
            Skill.getLearnableSkills(except, filter)
                .shuffled()
                .take(3)
                .takeUnless { it.isEmpty() }
                ?.let {
                    SkillChoice(
                        it.getOrElse(0) { Skill.EMPTY },
                        it.getOrElse(1) { Skill.EMPTY },
                        it.getOrElse(2) { Skill.EMPTY },
                    )
                } ?: EMPTY
    }
}