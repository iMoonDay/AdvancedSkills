package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.component.choice.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*

data class ChoiceData(
    private var choice: Choice = Choice.EMPTY,
    var refreshableCount: Int = 0,
    var count: Int = 0
) {

    fun next(player: PlayerEntity) {
        if (hasNext(player)) {
            count--
            choice = Choice.generate(player)
        } else {
            choice = Choice.EMPTY
        }
    }

    fun hasNext(player: PlayerEntity) = count > 0 && Choice.canGenerate(player)

    fun isCompleted() = count > 0 && choice.isEmpty()

    fun clear() {
        choice = Choice.EMPTY
        refreshableCount = 0
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
        if (refreshableCount <= 0 && !force || choice.isEmpty() || !Choice.canGenerate(player)) return
        refreshableCount--
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
        if (!choice.isEmpty() && choice.choices.any { invalidCheck(player, it) }) {
            choice = choice.replaceWith({ invalidCheck(player, it) }) { createChoosable(player, it) }
            modified = true
        }
        return modified
    }

    private fun createChoosable(
        player: PlayerEntity,
        except: MutableSet<Choosable>
    ): Choosable = SkillGenerator.generateSingle(
        player = player,
        exceptSkill = { createSkillFilter(except, it, player) },
        exceptEnhancement = { skill, enhancement -> createEnhancementFilter(except, skill, enhancement, player) },
    ).also {
        if (!it.isEmpty()) {
            except.add(it)
        }
    }

    private fun createEnhancementFilter(
        except: MutableSet<Choosable>,
        skill: Skill,
        enhancement: Enhancement,
        player: PlayerEntity
    ) = !skill.settings.drawable
        || except.any { !it.compatibleWith(EnhancementChoice(skill, enhancement.id)) }
        || player.isMaxEnhancement(skill, enhancement.id)

    private fun createSkillFilter(
        except: MutableSet<Choosable>,
        skill: Skill,
        player: PlayerEntity
    ) = player.hasLearned(skill)
        || !skill.settings.drawable
        || except.any { !it.compatibleWith(SkillChoice(skill)) }

    fun toNbt(): NbtCompound = NbtCompound().apply {
        put("choice", choice.toNbt())
        putInt("refreshableCount", refreshableCount)
        putInt("count", count)
    }

    companion object {

        private val invalidCheck: (PlayerEntity, Choosable) -> Boolean = { player, choosable ->
            val skill = choosable.skill
            when (choosable) {
                is SkillChoice -> skill.disabled || player.hasLearned(skill) || !skill.settings.drawable
                is EnhancementChoice -> skill.disabled
                    || !player.hasLearned(skill)
                    || !skill.settings.drawable
                    || player.isMaxEnhancement(skill, choosable.enhancementId)

                else -> true
            }
        }

        @JvmStatic
        fun fromNbt(nbt: NbtCompound): ChoiceData {
            val choice = Choice.fromNbt(nbt.getCompound("choice"))
            val refreshableCount = if (nbt.contains("refreshed")) {
                if (nbt.getBoolean("refreshed")) 0 else 1
            } else {
                nbt.getInt("refreshableCount")
            }
            val remainingCount = nbt.getInt("count")
            return ChoiceData(choice, refreshableCount, remainingCount)
        }
    }
}
