package com.imoonday.advskills_re.component

import net.minecraft.nbt.*

abstract class Choice<T>(
    val first: T,
    val second: T,
    val third: T,
) {

    abstract fun create(first: T, second: T, third: T): Choice<T>

    abstract fun isEmpty(item: T): Boolean

    abstract val emptyChoice: Choice<T>

    abstract val emptyItem: T

    fun isEmpty(): Boolean = this === emptyChoice || choices.all { isEmpty(it) }

    fun hasEmpty(): Boolean = this === emptyChoice || choices.any { isEmpty(it) }

    open fun hasDuplicates() = !isEmpty() && choices.distinct().size < choices.size

    val choices = listOf(first, second, third)

    fun withFirst(item: T): Choice<T> = create(item, second, third)

    fun withSecond(item: T): Choice<T> = create(first, item, third)

    fun withThird(item: T): Choice<T> = create(first, second, item)

    fun replaceWith(filter: (T) -> Boolean, generator: (except: MutableSet<T>) -> T): Choice<T> {
        var choice = this
        val except = choices.toMutableSet()
        if (filter(first)) choice = choice.withFirst(generator(except))
        if (filter(second)) choice = choice.withSecond(generator(except))
        if (filter(third)) choice = choice.withThird(generator(except))
        return choice
    }

    abstract fun toNbt(): NbtCompound

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Choice<*>) return false

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

    open fun removeDuplicates(): Choice<T> {
        if (!hasDuplicates()) return this
        val distinctSkills = choices.distinct()
        return when (distinctSkills.size) {
            1 -> create(distinctSkills[0], emptyItem, emptyItem)
            2 -> create(distinctSkills[0], distinctSkills[1], emptyItem)
            else -> this
        }
    }
}