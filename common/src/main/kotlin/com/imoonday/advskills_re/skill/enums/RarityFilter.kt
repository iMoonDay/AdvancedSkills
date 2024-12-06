package com.imoonday.advskills_re.skill.enums

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.text.*

enum class RarityFilter(private val rarity: SkillRarity?) : (Skill) -> Boolean {
    NONE(null),
    COMMON(SkillRarity.COMMON),
    UNCOMMON(SkillRarity.UNCOMMON),
    RARE(SkillRarity.RARE),
    SUPERB(SkillRarity.SUPERB),
    EPIC(SkillRarity.EPIC),
    LEGENDARY(SkillRarity.LEGENDARY),
    MYTHIC(SkillRarity.MYTHIC),
    UNIQUE(SkillRarity.UNIQUE);

    override fun invoke(skill: Skill): Boolean = rarity == null || skill.rarity == rarity

    val displayName: Text = rarity?.displayName ?: translate("rarityFilter.none")

    fun next(): RarityFilter = entries[(ordinal + 1) % entries.size]

    fun previous(): RarityFilter = entries[(ordinal + entries.size - 1) % entries.size]
}