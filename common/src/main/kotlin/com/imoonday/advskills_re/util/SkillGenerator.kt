package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.component.choice.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.entity.player.*

object SkillGenerator {

    @JvmStatic
    fun generateSingle(
        player: PlayerEntity,
        exceptSkill: (Skill) -> Boolean = { false },
        exceptEnhancement: (Skill, Enhancement) -> Boolean = { _, _ -> false }
    ): Choosable =
        createPool(player, exceptSkill, exceptEnhancement).drawSingle()?.let { createChoice(it) } ?: Choosable.EMPTY

    @JvmStatic
    fun generateChoice(player: PlayerEntity): Choice =
        createPool(player).drawMultiple(3, Choosable.EMPTY).map { createChoice(it) }.let {
            Choice(it[0], it[1], it[2])
        }

    private fun createPool(
        player: PlayerEntity,
        exceptSkill: (Skill) -> Boolean = { false },
        exceptEnhancement: (Skill, Enhancement) -> Boolean = { _, _ -> false }
    ): DynamicDrawPool<Skill, Pair<Skill, Enhancement>> {
        val learnedSkills = player.learnedSkills.filter { it.settings.drawable }

        return DynamicDrawPool(
            primaryItems = Skills.getLearnableSkills(learnedSkills),
            secondaryItems = generateEnhancements(learnedSkills),
            getPrimaryWeight = { it.weight },
            getSecondaryWeight = { getEnhancementWeight(it, player) },
            primaryRatio = 8.0,
            secondaryRatio = 2.0,
            primarySkipCondition = { shouldSkip(it, player) || exceptSkill(it) },
            secondarySkipCondition = {
                val skill = it.first
                val enhancement = it.second
                shouldSkip(skill, enhancement, player) || exceptEnhancement(skill, enhancement)
            }
        )
    }

    private fun generateEnhancements(skills: Collection<Skill>): List<Pair<Skill, Enhancement>> =
        skills.flatMap { skill -> skill.getAvailableEnhancements().map { enhancement -> skill to enhancement } }

    private fun getEnhancementWeight(pair: Pair<Skill, Enhancement>, player: PlayerEntity): Int {
        val (skill, enhancement) = pair
        val level = player.getEnhancementLvl(skill, enhancement.id)
        return enhancement.weight.getWeight(level + 1)
    }

    private fun shouldSkip(skill: Skill, player: PlayerEntity): Boolean =
        player.hasLearned(skill) || !skill.settings.drawable

    private fun shouldSkip(skill: Skill, enhancement: Enhancement, player: PlayerEntity): Boolean =
        shouldSkip(skill, player) || player.isMaxEnhancement(skill, enhancement.id)

    private fun createChoice(item: Any): Choosable {
        when (item) {
            is Skill -> return SkillChoice(item)
            is Pair<*, *> -> {
                val skill = item.first
                val enhancement = item.second
                if (skill is Skill && enhancement is Enhancement) {
                    return EnhancementChoice(skill, enhancement.id)
                }
            }
        }

        return Choosable.EMPTY
    }
}