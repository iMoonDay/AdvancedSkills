package com.imoonday.util

import com.imoonday.skill.Skill
import net.minecraft.nbt.NbtCompound

class LearnableSkillData(
    private var choice: SkillChoice = SkillChoice.EMPTY,
    var refreshed: Boolean = false,
    var count: Int = 0,
) {

    val first
        get() = choice.first
    val second
        get() = choice.second
    val third
        get() = choice.third

    fun next(except: Collection<Skill> = emptyList(), filter: (Skill) -> Boolean = { true }) {
        if (hasNext()) {
            count--
            choice = SkillChoice.generate(except, filter)
        } else {
            choice = SkillChoice.EMPTY
        }
        refreshed = false
    }

    fun hasNext() = count > 0
    fun clear() {
        choice = SkillChoice.EMPTY
        refreshed = false
    }

    fun reset() {
        count = 0
        clear()
    }

    fun get() = choice
    fun isEmpty() = choice.isEmpty()

    fun refresh(
        force: Boolean = false,
        except: Collection<Skill> = emptyList(),
        filter: (Skill) -> Boolean = { true },
    ) {
        if (refreshed && !force || choice.isEmpty() || !SkillChoice.canGenerate(except, filter)) return
        refreshed = true
        choice = SkillChoice.generate(except, filter)
    }

    fun correct(except: Collection<Skill> = emptyList(), filter: (Skill) -> Boolean = { true }): Boolean {
        var modified = false
        if (count < 0) {
            count = 0
            modified = true
        }
        if (choice.isEmpty() && hasNext()) {
            next(except, filter)
            modified = true
        }
//        if (choice.skills.any { it.invalid || it in except || !filter(it) }
//            && SkillChoice.canGenerate(except, filter)) {
//            choice = choice.replaceWith({ it.invalid || it in except || !filter(it) }) {
//                Skill.random(except.intersect(it), filter)
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

        fun fromNbt(nbt: NbtCompound): LearnableSkillData {
            val choice = SkillChoice.fromNbt(nbt.getCompound("choice"))
            val refreshed = nbt.getBoolean("refreshed")
            val remainingCount = nbt.getInt("count")
            return LearnableSkillData(choice, refreshed, remainingCount)
        }
    }
}
