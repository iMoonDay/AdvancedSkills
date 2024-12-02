package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.nbt.*

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
        if (hasNext() && SkillChoice.canGenerate(except, filter)) {
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

    fun correct(
        except: Collection<Skill> = emptyList(),
        filter: (Skill) -> Boolean = { true }
    ): Boolean {
        var modified = false
        if (count < 0) {
            count = 0
            modified = true
        }
        if (choice.isEmpty() && hasNext() && SkillChoice.canGenerate(except, filter)) {
            next(except, filter)
            modified = true
        }
        if (choice.hasDuplicates()) {
            choice = choice.removeDuplicates()
            modified = true
        }
        val replacePredicate: (Skill) -> Boolean = { it.invalid || it in except || !filter(it) }
        if (!choice.isEmpty() && choice.skills.any(replacePredicate)) {
            choice = choice.replaceWith(replacePredicate) { set ->
                Skills.random(except + set, filter).also {
                    if (!it.isEmpty()) {
                        set.add(it)
                    }
                }
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

        fun fromNbt(nbt: NbtCompound): LearnableSkillData {
            val choice = SkillChoice.fromNbt(nbt.getCompound("choice"))
            val refreshed = nbt.getBoolean("refreshed")
            val remainingCount = nbt.getInt("count")
            return LearnableSkillData(choice, refreshed, remainingCount)
        }
    }
}
