package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.skill.enums.*
import kotlinx.serialization.*
import net.minecraft.nbt.*

@Serializable
data class SkillModifier(
    var cooldown: Int?,
    var rarity: SkillRarity?,
    var time: Int?,
) {

    val isEmpty: Boolean
        get() = cooldown == null && rarity == null && time == null

    fun toNbt(): NbtCompound = NbtCompound().apply {
        if (cooldown != null) {
            putInt("cooldown", cooldown!!)
        }
        if (rarity != null) {
            putInt("rarity", rarity!!.ordinal)
        }
        if (time != null) {
            putInt("time", time!!)
        }
    }

    companion object {

        val EMPTY = SkillModifier(null, null, null)

        fun fromNbt(nbt: NbtCompound): SkillModifier {
            val cooldown = if (nbt.contains("cooldown")) nbt.getInt("cooldown") else null
            val rarity = if (nbt.contains("rarity")) SkillRarity.entries[nbt.getInt("rarity")] else null
            val time = if (nbt.contains("time")) nbt.getInt("time") else null
            return SkillModifier(cooldown, rarity, time)
        }
    }
}
