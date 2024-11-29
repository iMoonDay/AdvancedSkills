package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.skill.*
import net.minecraft.nbt.*
import net.minecraft.world.*

data class SkillChoice(
    val first: Skill,
    val second: Skill,
    val third: Skill,
) {

    fun isEmpty(world: World? = null) = first.isInvalid(world) && second.isInvalid(world) && third.isInvalid(world)

    fun hasEmpty(world: World? = null) = first.isInvalid(world) || second.isInvalid(world) || third.isInvalid(world)

    val skills = listOf(first, second, third)

    fun withFirst(skill: Skill): SkillChoice = SkillChoice(skill, second, third)

    fun withSecond(skill: Skill): SkillChoice = SkillChoice(first, skill, third)

    fun withThird(skill: Skill): SkillChoice = SkillChoice(first, second, skill)

    fun replaceWith(world: World?, predicate: (Skill) -> Boolean, generator: (Set<Skill>) -> Skill): SkillChoice {
        var choice = this
        if (predicate(first)) choice = choice.withFirst(generator(choice.getNoEmpty(world, first)))
        if (predicate(second)) choice = choice.withSecond(generator(choice.getNoEmpty(world, second)))
        if (predicate(third)) choice = choice.withThird(generator(choice.getNoEmpty(world, third)))
        return choice
    }

    fun getNoEmpty(world: World? = null, except: Skill? = null) =
        skills.filterNot { it == except || it.isInvalid(world) }.toSet()

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
        val EMPTY = SkillChoice(Skills.EMPTY, Skills.EMPTY, Skills.EMPTY)

        fun fromNbt(nbt: NbtCompound): SkillChoice = SkillChoice(
            Skills.fromId(nbt.getString("1")),
            Skills.fromId(nbt.getString("2")),
            Skills.fromId(nbt.getString("3")),
        )

        fun canGenerate(
            world: World? = null,
            except: Collection<Skill> = emptyList(),
            filter: (Skill) -> Boolean = { true }
        ) = Skills.getLearnableSkills(world, except, filter).isNotEmpty()

        fun generate(
            world: World? = null,
            except: Collection<Skill> = emptyList(),
            filter: (Skill) -> Boolean = { true }
        ): SkillChoice =
            Skills.getLearnableSkills(world, except, filter)
                .shuffled()
                .take(3)
                .takeUnless { it.isEmpty() }
                ?.let {
                    SkillChoice(
                        it.getOrElse(0) { Skills.EMPTY },
                        it.getOrElse(1) { Skills.EMPTY },
                        it.getOrElse(2) { Skills.EMPTY },
                    )
                } ?: EMPTY
    }
}