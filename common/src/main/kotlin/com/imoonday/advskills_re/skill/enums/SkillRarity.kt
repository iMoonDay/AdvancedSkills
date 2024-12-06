package com.imoonday.advskills_re.skill.enums

import com.imoonday.advskills_re.util.*
import net.minecraft.text.*
import net.minecraft.util.*

enum class SkillRarity(
    val level: Int,
    val weight: Int,
    val id: String,
    val roman: String,
    val formatting: Formatting,
) {

    USELESS(0, 0, "useless", "N", Formatting.GRAY),
    COMMON(1, 8, "common", "I", Formatting.WHITE),
    UNCOMMON(2, 7, "uncommon", "II", Formatting.GREEN),
    RARE(3, 6, "rare", "III", Formatting.AQUA),
    SUPERB(4, 5, "superb", "IV", Formatting.GOLD),
    EPIC(5, 4, "epic", "V", Formatting.RED),
    LEGENDARY(6, 3, "legendary", "VI", Formatting.LIGHT_PURPLE),
    MYTHIC(7, 2, "mythic", "VII", Formatting.DARK_PURPLE),
    UNIQUE(8, 1, "unique", "VIII", Formatting.DARK_RED);

    val displayName: Text
        get() = translate("skillRarity.$id")

    val color: Int
        get() = formatting.colorValue!!

    companion object {

        @JvmStatic
        fun fromLevel(level: Int): SkillRarity? = entries.find { it.level == level }

        @JvmStatic
        fun fromId(id: String): SkillRarity? = entries.find { it.id == id }

        @JvmStatic
        fun parse(string: String): SkillRarity? = try {
            valueOf(string)
        } catch (e: Exception) {
            fromId(string) ?: string.toIntOrNull()?.let { fromLevel(it) }
        }
    }
}