package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.skill.*
import net.minecraft.text.*

enum class RarityFilter(private val rarity: Skill.Rarity?) : (Skill) -> Boolean {
    NONE(null),
    COMMON(Skill.Rarity.COMMON),
    UNCOMMON(Skill.Rarity.UNCOMMON),
    RARE(Skill.Rarity.RARE),
    SUPERB(Skill.Rarity.SUPERB),
    EPIC(Skill.Rarity.EPIC),
    LEGENDARY(Skill.Rarity.LEGENDARY),
    MYTHIC(Skill.Rarity.MYTHIC),
    UNIQUE(Skill.Rarity.UNIQUE);

    override fun invoke(skill: Skill): Boolean = rarity == null || skill.rarity == rarity

    val displayName: Text = rarity?.displayName ?: translate("rarityFilter.none")

    fun next(): RarityFilter = entries[(ordinal + 1) % entries.size]

    fun previous(): RarityFilter = entries[(ordinal + entries.size - 1) % entries.size]
}