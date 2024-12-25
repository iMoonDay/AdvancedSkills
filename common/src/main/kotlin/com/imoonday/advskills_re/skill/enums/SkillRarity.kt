package com.imoonday.advskills_re.skill.enums

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.util.*
import kotlinx.serialization.*
import net.minecraft.text.*
import net.minecraft.util.*

@Serializable
enum class SkillRarity(
    val level: Int,
    val id: String,
    val roman: String,
    val formatting: Formatting,
) {

    @SerialName("useless")
    USELESS(0, "useless", "N", Formatting.GRAY),

    @SerialName("common")
    COMMON(1, "common", "I", Formatting.WHITE),

    @SerialName("uncommon")
    UNCOMMON(2, "uncommon", "II", Formatting.GREEN),

    @SerialName("rare")
    RARE(3, "rare", "III", Formatting.AQUA),

    @SerialName("superb")
    SUPERB(4, "superb", "IV", Formatting.GOLD),

    @SerialName("epic")
    EPIC(5, "epic", "V", Formatting.RED),

    @SerialName("legendary")
    LEGENDARY(6, "legendary", "VI", Formatting.LIGHT_PURPLE),

    @SerialName("mythic")
    MYTHIC(7, "mythic", "VII", Formatting.DARK_PURPLE),

    @SerialName("unique")
    UNIQUE(8, "unique", "VIII", Formatting.DARK_RED);

    var weight: Int
        get() = GlobalConfig.get().getRarityWeight(this)
        set(value) {
            GlobalConfig.get().setRarityWeight(this, value)
        }
    val displayName: Text
        get() = translate("skillRarity.$id")

    val color: Int
        get() = formatting.colorValue!!

    companion object {

        @JvmStatic
        val DEFAULT_WEIGHTS = mapOf(
            COMMON to 8,
            UNCOMMON to 7,
            RARE to 6,
            SUPERB to 5,
            EPIC to 4,
            LEGENDARY to 3,
            MYTHIC to 2,
            UNIQUE to 1
        )

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