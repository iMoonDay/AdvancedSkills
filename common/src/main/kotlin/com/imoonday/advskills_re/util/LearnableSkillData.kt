package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.skill.*
import net.minecraft.nbt.*
import net.minecraft.world.*

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

    fun next(world: World?, except: Collection<Skill> = emptyList(), filter: (Skill) -> Boolean = { true }) {
        if (hasNext()) {
            count--
            choice = SkillChoice.generate(world, except, filter)
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
        world: World?,
        force: Boolean = false,
        except: Collection<Skill> = emptyList(),
        filter: (Skill) -> Boolean = { true },
    ) {
        if (refreshed && !force || choice.isEmpty() || !SkillChoice.canGenerate(world, except, filter)) return
        refreshed = true
        choice = SkillChoice.generate(world, except, filter)
    }

    fun correct(
        world: World?,
        except: Collection<Skill> = emptyList(),
        filter: (Skill) -> Boolean = { true }
    ): Boolean {
        var modified = false
        if (count < 0) {
            count = 0
            modified = true
        }
        if (choice.isEmpty() && hasNext()) {
            next(world, except, filter)
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
