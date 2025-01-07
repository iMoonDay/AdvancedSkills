package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.component.choice.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*

data class Choice(
    val first: Choosable,
    val second: Choosable,
    val third: Choosable,
) {

    val choices = listOf(first, second, third)

    fun isEmpty(): Boolean = this === EMPTY || choices.all { it.isEmpty() }

    fun hasEmpty(): Boolean = this === EMPTY || choices.any { it.isEmpty() }

    fun hasDuplicates(): Boolean {
        if (isEmpty()) return false

        for (i in choices.indices) {
            for (j in i + 1 until choices.size) {
                if (!Companion.areCompatible(choices[i], choices[j])) {
                    return true
                }
            }
        }

        return false
    }

    fun withFirst(item: Choosable): Choice = Choice(item, second, third)

    fun withSecond(item: Choosable): Choice = Choice(first, item, third)

    fun withThird(item: Choosable): Choice = Choice(first, second, item)

    fun replaceWith(filter: (Choosable) -> Boolean, generator: (except: MutableSet<Choosable>) -> Choosable): Choice {
        var choice = this
        val except = choices.toMutableSet()
        if (filter(first)) choice = choice.withFirst(generator(except))
        if (filter(second)) choice = choice.withSecond(generator(except))
        if (filter(third)) choice = choice.withThird(generator(except))
        return choice
    }

    fun toNbt(): NbtCompound = NbtCompound().apply {
        put("1", first.toNbt())
        put("2", second.toNbt())
        put("3", third.toNbt())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Choice) return false

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

    fun removeDuplicates(): Choice {
        if (isEmpty()) return this

        val unique = mutableListOf<Choosable>()
        for (choice in choices) {
            if (unique.none { !it.compatibleWith(choice) }) {
                unique.add(choice)
            }
        }

        return when (unique.size) {
            0 -> EMPTY
            1 -> Choice(unique.first(), Choosable.EMPTY, Choosable.EMPTY)
            2 -> Choice(unique.first(), unique.last(), Choosable.EMPTY)
            3 -> Choice(unique.first(), unique[1], unique.last())
            else -> throw IllegalStateException("Choice has more than 3 items")
        }
    }

    companion object {

        val EMPTY: Choice = Choice(Choosable.EMPTY, Choosable.EMPTY, Choosable.EMPTY)

        @JvmStatic
        fun fromNbt(nbt: NbtCompound): Choice = Choice(
            Choosable.parse(nbt.getCompound("1")),
            Choosable.parse(nbt.getCompound("2")),
            Choosable.parse(nbt.getCompound("3")),
        )

        @JvmStatic
        fun canGenerate(player: PlayerEntity): Boolean {
            if (Skills.getLearnableSkills(player.learnedSkills).isNotEmpty()) {
                return true
            }
            return player.learnedSkills
                .map { it to it.availableEnhancements }
                .any { pair ->
                    pair.second.any {
                        player.getEnhancementLvl(pair.first, it.id) < it.maxLevel
                    }
                }
        }

        @JvmStatic
        fun generate(player: PlayerEntity): Choice = SkillGenerator.generateChoice(player)

        @JvmStatic
        fun areCompatible(choosable: Choosable, another: Choosable): Boolean =
            choosable.compatibleWith(another) && another.compatibleWith(choosable)
    }
}