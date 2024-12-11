package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.enhancement.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*

class EnhancementChoice(
    first: Pair,
    second: Pair,
    third: Pair,
) : Choice<EnhancementChoice.Pair>(first, second, third) {

    override fun create(
        first: Pair,
        second: Pair,
        third: Pair
    ): Choice<Pair> = EnhancementChoice(first, second, third)

    override fun isEmpty(item: Pair): Boolean = item.isEmtpy()

    override val emptyChoice: Choice<Pair> = EMPTY

    override val emptyItem: Pair = Pair.EMPTY

    override fun toNbt(): NbtCompound = NbtCompound().apply {
        put("1", first.toNbt())
        put("2", second.toNbt())
        put("3", third.toNbt())
    }

    override fun hasDuplicates(): Boolean =
        super.hasDuplicates()
            || first.isSameSkillSameType(second)
            || first.isSameSkillSameType(third)
            || second.isSameSkillSameType(third)

    override fun removeDuplicates(): Choice<Pair> {
        if (!hasDuplicates()) return this
        val distinctSkills = choices.distinctBy { it.skill to it.enhancement.type }
        return when (distinctSkills.size) {
            1 -> create(distinctSkills[0], emptyItem, emptyItem)
            2 -> create(distinctSkills[0], distinctSkills[1], emptyItem)
            else -> this
        }
    }

    data class Pair(
        val skill: Skill,
        val enhancement: SkillEnhancement
    ) {

        fun isEmtpy(): Boolean = this === EMPTY || skill.invalid || enhancement.isEmpty()

        fun isSameSkillSameType(other: Pair): Boolean =
            skill == other.skill && enhancement.isOf(other.enhancement.type)

        fun toNbt(): NbtCompound = NbtCompound().apply {
            putString("skill", skill.id.toString())
            put("enhancement", enhancement.save())
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Pair) return false
            if (skill != other.skill) return false
            return enhancement == other.enhancement
        }

        override fun hashCode(): Int {
            var result = skill.hashCode()
            result = 31 * result + enhancement.hashCode()
            return result
        }

        override fun toString(): String = "SkillEnhancementPair(skill=$skill, enhancement=$enhancement)"

        fun copy(): Pair = Pair(skill, enhancement.copy())

        companion object {

            val EMPTY = Pair(Skills.EMPTY, EmptyEnhancement.get())

            fun fromNbt(nbt: NbtCompound): Pair =
                Pair(
                    Skills.fromId(nbt.getString("skill")),
                    SkillEnhancementType.create(nbt.getCompound("enhancement"))
                )
        }
    }

    companion object {

        @JvmField
        val EMPTY =
            EnhancementChoice(Pair.EMPTY, Pair.EMPTY, Pair.EMPTY)

        @JvmStatic
        fun fromNbt(nbt: NbtCompound): EnhancementChoice = EnhancementChoice(
            Pair.fromNbt(nbt.getCompound("1")),
            Pair.fromNbt(nbt.getCompound("2")),
            Pair.fromNbt(nbt.getCompound("3"))
        )

        @JvmStatic
        fun canGenerate(player: PlayerEntity): Boolean {
            for (skill in player.learnedSkills) {
                for (enhancement in skill.availableEnhancements) {
                    val enhancement1 = player.getEnhancement(skill, enhancement)
                    if (enhancement1 == null || !enhancement1.isMaxLevel()) {
                        return true
                    }
                }
            }
            return false
        }

        @JvmStatic
        fun generate(player: PlayerEntity): EnhancementChoice = getLearnableEnhancements(player).randomByWeight(
            weightMapper = { it.skill.weight + (it.enhancement.type.maxLevel - it.enhancement.level).coerceAtLeast(0) * 2 },
            defaultValue = Pair.EMPTY,
            count = 3
        ).let { EnhancementChoice(it[0], it[1], it[2]) }

        @JvmStatic
        fun getLearnableEnhancements(
            player: PlayerEntity,
            except: Collection<Pair> = emptyList()
        ): List<Pair> {
            val choices = mutableListOf<Pair>()
            for (skill in player.learnedSkills) {
                for (enhancement in skill.availableEnhancements) {
                    val enhancement1 = player.getEnhancement(skill, enhancement)
                    if (enhancement1 == null) {
                        val pair = Pair(skill, enhancement.create())

                        if (except.any { pair.isSameSkillSameType(it) }) continue
                        if (pair.enhancement.level > enhancement.maxLevel) continue

                        choices.add(pair)
                    } else if (!enhancement1.isMaxLevel()) {
                        val pair = Pair(skill, enhancement1.copyWithLevel(enhancement1.level + 1))

                        if (except.any { pair.isSameSkillSameType(it) }) continue
                        if (pair.enhancement.level > enhancement.maxLevel) continue

                        choices.add(pair)
                    }
                }
            }
            return choices
        }

        @JvmStatic
        fun random(player: PlayerEntity, except: Collection<Pair> = emptyList()): Pair =
            getLearnableEnhancements(player, except).randomByWeight(
                weightMapper = { it.skill.weight + (it.enhancement.type.maxLevel - it.enhancement.level).coerceAtLeast(0) * 2 },
                defaultValue = Pair.EMPTY,
            )
    }
}