package com.imoonday.advskills_re.config

import com.imoonday.advskills_re.util.*
import net.minecraft.nbt.*
import net.minecraft.util.*

class SkillConfig {

    var skillCooldownMultiplier: Double = 1.0
    var skillXpMultiplier: Double = 1.0
    var skillModifier: MutableMap<String, SkillModifier> = mutableMapOf()
    var skillBlackList: MutableSet<String> = mutableSetOf()
    var defaultSkillSlots: MutableMap<String, Int> = SkillContainer.DEFAULT_SLOTS.toMutableMap()

    fun getModifier(id: Identifier): SkillModifier? = skillModifier[id.toString()]

    fun getOrCreateModifier(id: Identifier): SkillModifier =
        skillModifier.getOrPut(id.toString()) { SkillModifier.EMPTY }

    fun removeModifier(id: Identifier): Boolean = skillModifier.remove(id.toString()) != null

    fun isInBlackList(id: Identifier): Boolean = skillBlackList.contains(id.toString())

    fun addBlackList(id: Identifier) {
        skillBlackList.add(id.toString())
    }

    fun removeBlackList(id: Identifier): Boolean = skillBlackList.remove(id.toString())

    fun getDefaultSkillSlots(slot: String): Int = defaultSkillSlots[slot] ?: 0

    fun setDefaultSkillSlot(slot: String, count: Int) {
        defaultSkillSlots[slot] = count
    }

    fun setDefaultActiveSkillSlot(count: Int) {
        defaultSkillSlots["active"] = count
    }

    fun setDefaultGenericSkillSlot(count: Int) {
        defaultSkillSlots["generic"] = count
    }

    fun setDefaultPassiveSkillSlot(count: Int) {
        defaultSkillSlots["passive"] = count
    }

    fun fromTag(tag: NbtCompound) {
        skillModifier.clear()
        skillBlackList.clear()
        defaultSkillSlots.clear()

        val skillModifierTag = tag.getCompound("skillModifier")
        for (id in skillModifierTag.keys) {
            val modifierTag = skillModifierTag.getCompound(id)
            skillModifier[id] = SkillModifier.fromNbt(modifierTag)
        }

        val skillBlackListTag = tag.getList("skillBlackList", NbtElement.STRING_TYPE.toInt())
        skillBlackListTag.forEach {
            skillBlackList.add(it.asString())
        }

        val defaultSkillSlotsTag = tag.getCompound("defaultSkillSlots")
        for (namespace in defaultSkillSlotsTag.keys) {
            defaultSkillSlots[namespace] = defaultSkillSlotsTag.getInt(namespace)
        }

        skillCooldownMultiplier = tag.getDouble("skillCooldownMultiplier")

        skillXpMultiplier = tag.getDouble("skillXpMultiplier")
    }

    fun toTag(tag: NbtCompound): NbtCompound {
        return tag.apply {
            put("skillModifier", NbtCompound().apply {
                for ((id, modifier) in skillModifier) {
                    put(id, modifier.toNbt())
                }
            })
            put("skillBlackList", NbtList().apply {
                skillBlackList.forEach {
                    add(NbtString.of(it))
                }
            })
            put("defaultSkillSlots", NbtCompound().apply {
                for ((slot, count) in defaultSkillSlots) {
                    putInt(slot, count)
                }
            })
            putDouble("skillCooldownMultiplier", skillCooldownMultiplier)
            putDouble("skillXpMultiplier", skillXpMultiplier)
        }
    }

    companion object {

        var instance = SkillConfig()
    }
}
