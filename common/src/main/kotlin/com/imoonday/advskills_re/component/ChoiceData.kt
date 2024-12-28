package com.imoonday.advskills_re.component

import net.minecraft.entity.player.*
import net.minecraft.nbt.*

data class ChoiceData(
    private var choice: Choice = Choice.EMPTY,
    var refreshed: Boolean = false,
    var count: Int = 0,
) {

    fun next(player: PlayerEntity) {
        if (hasNext(player)) {
            count--
            choice = Choice.generate(player)
        } else {
            choice = Choice.EMPTY
        }
        refreshed = false
    }

    fun hasNext(player: PlayerEntity) = count > 0 && Choice.canGenerate(player)

    fun clear() {
        choice = Choice.EMPTY
        refreshed = false
    }

    fun reset() {
        count = 0
        clear()
    }

    fun get() = choice

    fun isEmpty() = choice.isEmpty()

    fun refresh(
        player: PlayerEntity,
        force: Boolean = false,
    ) {
        if (refreshed && !force || choice.isEmpty() || !Choice.canGenerate(player)) return
        refreshed = true
        choice = Choice.generate(player)
    }

    fun correct(player: PlayerEntity): Boolean {
        var modified = false
        if (count < 0) {
            count = 0
            modified = true
        }
        if (choice.isEmpty() && hasNext(player)) {
            next(player)
            modified = true
        }
        if (choice.hasDuplicates()) {
            choice = choice.removeDuplicates()
            modified = true
        }
//        val replacePredicate: (Skill) -> Boolean = { it.invalid || it in except || !filter(it) }
//        if (!choice.isEmpty() && choice.choices.any(replacePredicate)) {
//            choice = choice.replaceWith(replacePredicate) { set ->
//                Skills.random(except + set, filter).also { if (!it.isEmpty()) set.add(it) }
//            }
//            modified = true
//        }

//        val replacePredicate: (EnhancementChoice.Pair) -> Boolean =
//            {
//                it.isEmtpy() || player.getEnhancement(it.skill, it.enhancement.type)
//                    ?.run { it.enhancement.level <= level } == true
//            }
//        if (!choice.isEmpty() && choice.choices.any(replacePredicate)) {
//            choice = choice.replaceWith(replacePredicate) { set ->
//                EnhancementChoice.random(player, set).also { if (!it.isEmtpy()) set.add(it) }
//            }
//            modified = true
//        }
        return modified
    }

    fun toNbt(): NbtCompound = NbtCompound().apply {
        put("choice", choice.toNbt())
        putBoolean("refreshed", refreshed)
        putInt("count", count)
    }

    companion object {

        fun fromNbt(nbt: NbtCompound): ChoiceData {
            val choice = Choice.fromNbt(nbt.getCompound("choice"))
            val refreshed = nbt.getBoolean("refreshed")
            val remainingCount = nbt.getInt("count")
            return ChoiceData(choice, refreshed, remainingCount)
        }
    }
}
