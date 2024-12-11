package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*

class LearnableEnhancementData(
    private var choice: Choice<EnhancementChoice.Pair> = EnhancementChoice.EMPTY,
    var refreshed: Boolean = false,
    var count: Int = 0,
) {

    val first
        get() = choice.first
    val second
        get() = choice.second
    val third
        get() = choice.third

    fun next(player: PlayerEntity) {
        if (hasNext() && EnhancementChoice.canGenerate(player)) {
            count--
            choice = EnhancementChoice.generate(player)
        } else {
            choice = EnhancementChoice.EMPTY
        }
        refreshed = false
    }

    fun hasNext() = count > 0

    fun clear() {
        choice = EnhancementChoice.EMPTY
        refreshed = false
    }

    fun reset() {
        count = 0
        clear()
    }

    fun get() = choice

    fun isEmpty() = choice.isEmpty()

    fun refresh(player: PlayerEntity, force: Boolean = false) {
        if (refreshed && !force || choice.isEmpty() || !EnhancementChoice.canGenerate(player)) return
        refreshed = true
        choice = EnhancementChoice.generate(player)
    }

    fun correct(player: PlayerEntity): Boolean {
        var modified = false
        if (count < 0) {
            count = 0
            modified = true
        }
        if (choice.isEmpty() && hasNext() && EnhancementChoice.canGenerate(player)) {
            next(player)
            modified = true
        }
        if (choice.hasDuplicates()) {
            choice = choice.removeDuplicates()
            modified = true
        }
        val replacePredicate: (EnhancementChoice.Pair) -> Boolean =
            {
                it.isEmtpy() || player.getEnhancement(it.skill, it.enhancement.type)
                    ?.run { it.enhancement.level <= level } == true
            }
        if (!choice.isEmpty() && choice.choices.any(replacePredicate)) {
            choice = choice.replaceWith(replacePredicate) { set ->
                EnhancementChoice.random(player, set).also { if (!it.isEmtpy()) set.add(it) }
            }
            modified = true
        }
        return modified
    }

    fun toNbt(): NbtCompound = NbtCompound().apply {
        put("choice", choice.toNbt())
        putBoolean("refreshed", refreshed)
        putInt("count", count)
    }

    companion object {

        fun fromNbt(nbt: NbtCompound): LearnableEnhancementData {
            val choice = EnhancementChoice.fromNbt(nbt.getCompound("choice"))
            val refreshed = nbt.getBoolean("refreshed")
            val remainingCount = nbt.getInt("count")
            return LearnableEnhancementData(choice, refreshed, remainingCount)
        }
    }
}
