package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.component.choice.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.nbt.*

class EnhancementChoice(
    skill: Skill,
    val enhancementId: String
) : Choosable(skill) {

    override val type: Type = Type.ENHANCEMENT

    val enhancement: Enhancement?
        get() = skill.getEnhancement(enhancementId)

    override fun isEmpty(): Boolean = skill.disabled || enhancement == null

    override fun compatibleWith(other: Choosable): Boolean {
        if (other.type != this.type) return true
        if (other !is EnhancementChoice) return true
        if (other.skill != this.skill) return true
        return other.enhancementId != this.enhancementId
    }

    override fun toNbt(): NbtCompound = super.toNbt().apply {
        putString("enhancement", enhancementId)
    }

    override fun equals(other: Any?): Boolean =
        super.equals(other) && (other as? EnhancementChoice)?.enhancementId == this.enhancementId

    override fun hashCode(): Int = super.hashCode() * 31 + enhancementId.hashCode()

    override fun toString(): String = "EnhancementChoice(skill=$skill, enhancementId=$enhancementId, type=$type)"

    companion object {

        @JvmStatic
        fun fromNbt(nbt: NbtCompound): Choosable {
            val skill = Skills.fromIdNullable(nbt.getString("skill")) ?: return EMPTY
            val enhancementId = nbt.getString("enhancement")
            return EnhancementChoice(skill, enhancementId)
        }

//        @JvmStatic
//        fun canGenerate(player: PlayerEntity): Boolean {
//            for (skill in player.learnedSkills) {
//                for (enhancement in skill.availableEnhancements) {
//                    val enhancement1 = player.getEnhancement(skill, enhancement)
//                    if (enhancement1 == null || !enhancement1.isMaxLevel()) {
//                        return true
//                    }
//                }
//            }
//            return false
//        }

//        @JvmStatic
//        fun generate(player: PlayerEntity): Choice = getLearnableEnhancements(player).randomByWeight(
//            weightMapper = { it.skill.weight + (it.enhancement.type.maxLevel - it.enhancement.level).coerceAtLeast(0) * 2 },
//            defaultValue = Pair.EMPTY,
//            count = 3
//        ).let { Choice(it[0], it[1], it[2]) }
//
//        @JvmStatic
//        fun getLearnableEnhancements(
//            player: PlayerEntity
//        ): List<EnhancementChoice> {
//            val choices = mutableListOf<EnhancementChoice>()
//            for (skill in player.learnedSkills) {
//                for (enhancement in skill.availableEnhancements) {
//                    val enhancement1 = player.getEnhancement(skill, enhancement)
//                    if (enhancement1 == null) {
//                        val choice = EnhancementChoice(skill, enhancement.create())
//
//                        if (choices.any { !choice.compatibleWith(it) }) continue
//                        if (choice.enhancement.level > enhancement.maxLevel) continue
//
//                        choices.add(choice)
//                    } else if (!enhancement1.isMaxLevel()) {
//                        val choice = EnhancementChoice(skill, enhancement1.copyWithLevel(enhancement1.level + 1))
//
//                        if (choices.any { !choice.compatibleWith(it) }) continue
//                        if (choice.enhancement.level > enhancement.maxLevel) continue
//
//                        choices.add(choice)
//                    }
//                }
//            }
//            return choices
//        }
//
//        @JvmStatic
//        fun random(player: PlayerEntity, except: Collection<EnhancementChoice> = emptyList()): Choosable =
//            getLearnableEnhancements(player, except).randomByWeight(
//                weightMapper = { it.skill.weight + (it.enhancement.type.maxLevel - it.enhancement.level).coerceAtLeast(0) * 2 },
//                defaultValue = Choice.EMPTY,
//            )
    }
}