package com.imoonday.advskills_re.skill.enums

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.text.*
import java.text.*
import java.util.*

enum class SkillSorter : Comparator<Skill> {
    DEFAULT {

        override fun compare(o1: Skill?, o2: Skill?): Int = 0
    },
    ID {

        override fun compare(o1: Skill?, o2: Skill?): Int {
            if (o1 == null || o2 == null) return 0
            return o1.id.compareTo(o2.id)
        }
    },
    ID_REVERSE {

        override fun compare(o1: Skill?, o2: Skill?): Int {
            if (o1 == null || o2 == null) return 0
            return o2.id.compareTo(o1.id)
        }
    },
    NAME {

        override fun compare(o1: Skill?, o2: Skill?): Int {
            if (o1 == null || o2 == null) return 0
            val name1 = o1.name.string
            val name2 = o2.name.string
            val locale = if (isChinese(name1) || isChinese(name2)) Locale.CHINESE else Locale.getDefault()
            return Collator.getInstance(locale).compare(name1, name2)
        }
    },
    NAME_REVERSE {

        override fun compare(o1: Skill?, o2: Skill?): Int {
            if (o1 == null || o2 == null) return 0
            val name1 = o1.name.string
            val name2 = o2.name.string
            val locale = if (isChinese(name1) || isChinese(name2)) Locale.CHINESE else Locale.getDefault()
            return Collator.getInstance(locale).compare(o2.name.string, o1.name.string)
        }
    },
    RARITY {

        override fun compare(o1: Skill?, o2: Skill?): Int {
            if (o1 == null || o2 == null) return 0
            return o1.rarity.level.compareTo(o2.rarity.level)
        }
    },
    RARITY_REVERSE {

        override fun compare(o1: Skill?, o2: Skill?): Int {
            if (o1 == null || o2 == null) return 0
            return o2.rarity.level.compareTo(o1.rarity.level)
        }
    },
    COOLDOWN {

        override fun compare(o1: Skill?, o2: Skill?): Int {
            if (o1 == null || o2 == null) return 0
            return o1.cooldown.compareTo(o2.cooldown)
        }
    },
    COOLDOWN_REVERSE {

        override fun compare(o1: Skill?, o2: Skill?): Int {
            if (o1 == null || o2 == null) return 0
            return o2.cooldown.compareTo(o1.cooldown)
        }
    },
    UPDATE_TIME {

        override fun compare(o1: Skill?, o2: Skill?): Int {
            if (o1 == null || o2 == null) return 0
            return Skills.compareIndex(o1, o2)
        }
    },
    UPDATE_TIME_REVERSE {

        override fun compare(o1: Skill?, o2: Skill?): Int {
            if (o1 == null || o2 == null) return 0
            return Skills.compareIndex(o2, o1)
        }
    };

    val displayName: Text = translate("skillSort.${name.lowercase()}")

    fun next(): SkillSorter = entries[(ordinal + 1) % entries.size]

    fun previous(): SkillSorter = entries[(ordinal + entries.size - 1) % entries.size]

    protected fun isChinese(str: String): Boolean =
        client?.languageManager?.language?.let { it == "zh_cn" } ?: containsChinese(str)

    protected fun containsChinese(text: String): Boolean {
        val regex = "[\\u4e00-\\u9fa5]".toRegex()
        return regex.containsMatchIn(text)
    }
}