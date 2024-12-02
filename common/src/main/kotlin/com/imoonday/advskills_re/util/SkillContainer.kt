package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.mojang.logging.*
import net.minecraft.nbt.*
import org.slf4j.*

class SkillContainer(
    private val skills: LinkedHashMap<Skill, SkillData> = linkedMapOf(),
    private val slots: MutableMap<Int, SkillSlot> = createDefaultSlots(),
) {

    val skillSize: Int
        get() = skills.size
    val slotSize: Int
        get() = slots.size

    init {
        checkContinuous()
        if (slots.size > MAX_SLOT_SIZE) {
            slots.keys.removeIf { it > MAX_SLOT_SIZE }
        }
    }

    fun getAllSkills(predicate: (Skill, SkillData) -> Boolean = { _, _ -> true }): Set<Skill> =
        skills.filterNot { it.key.invalid }.filter { predicate(it.key, it.value) }.keys

    fun learn(
        skill: Skill,
        data: SkillData = SkillData(),
        resultCallback: (Boolean) -> Unit = {}
    ): Boolean =
        if (skills.containsKey(skill) || skill.invalid) {
            resultCallback(false)
            false
        } else {
            skills[skill] = data
            resultCallback(true)
            true
        }

    fun learnAll(callback: (skill: Skill) -> Unit = {}) {
        Skills.getLearnableSkills(skills.keys).forEach {
            skills[it] = SkillData()
            callback(it)
        }
    }

    fun forget(
        skill: Skill,
        resultCallback: (Boolean) -> Unit = {},
        unequipCallback: (slot: SkillSlot, result: Boolean) -> Unit = { _, _ -> },
    ): Boolean =
        if (skills.containsKey(skill)) {
            slots.forEach { (_, slot) ->
                unequipCallback(slot, slot.unequipIf({ it == skill }))
            }
            skills.remove(skill)
            resultCallback(true)
            true
        } else {
            resultCallback(false)
            false
        }

    fun forgetAll(unequipCallback: (slot: SkillSlot) -> Unit = { _ -> }) {
        slots.forEach { (_, slot) ->
            slot.unequip()
            unequipCallback(slot)
        }
        skills.clear()
    }

    fun getData(skill: Skill): SkillData? = skills[skill]

    fun forEachData(predicate: (SkillData) -> Boolean = { true }, action: (SkillData) -> Unit) =
        skills.values.filter(predicate).forEach(action)

    fun getSlot(index: Int) = slots[index]

    fun getSlot(skill: Skill): SkillSlot? = slots.values.find { it.skill == skill }

    fun getEmptySlot(skill: Skill? = null): SkillSlot? =
        slots.values.find { it.isEmpty() && (skill == null || it.canEquip(skill)) }

    fun getAllSlots(predicate: (SkillSlot) -> Boolean = { true }) = slots.values.sortedBy { it.index }.filter(predicate)

    fun findSlot(predicate: (SkillSlot) -> Boolean = { true }): SkillSlot? = slots.values.find(predicate)

    inline fun <reified T : SkillSlot> getSlotByType(): List<SkillSlot> = getAllSlots { T::class.isInstance(it) }

    fun getLastSlot(predicate: (SkillSlot) -> Boolean = { true }): SkillSlot? =
        slots.values.sortedByDescending { it.index }.find(predicate)

    fun getLastSlot(
        vararg predicates: (SkillSlot) -> Boolean = emptyArray(),
    ): SkillSlot? =
        slots.values.sortedByDescending { it.index }.run {
            for (it in predicates) {
                val slot = find(it) ?: continue
                return slot
            }
            null
        }

    /**
     * Can only have a maximum of 10 slots
     * @return the final index of the slot
     * */
    fun addSlot(slot: SkillSlot): Int? {
        if (slot.index <= 0 || slots.size >= MAX_SLOT_SIZE) return null
        return when {
            slots.containsKey(slot.index) -> {
                slots.keys.sortedDescending().filter { it >= slot.index }.forEach {
                    slots[it + 1] = slots[it]!!.copyWithIndex(it + 1)
                }
                slots[slot.index] = slot
                slot.index
            }

            slot.index - 1 !in slots -> {
                val maxNext = (slots.keys.maxOrNull() ?: 0) + 1
                slots[maxNext] = slot.copyWithIndex(maxNext)
                maxNext
            }

            else -> {
                slots[slot.index] = slot
                slot.index
            }
        }
    }

    /**
     * @return the removed slot
     * */
    fun removeSlot(index: Int): SkillSlot? {
        if (slots.containsKey(index)) {
            val removed = slots.remove(index)
            slots.filterKeys { it > index }.forEach { (currentIndex, currentSlot) ->
                slots.remove(currentIndex)
                slots[currentIndex - 1] = currentSlot.copyWithIndex(currentIndex - 1)
            }
            return removed
        }
        return null
    }

    fun resetSlots() {
        slots.clear()
        slots.putAll(createDefaultSlots())
    }

    private fun checkContinuous() {
        val sortedKeys = slots.keys.sorted()
        val continuousKeys = (1..sortedKeys.size).toList()
        if (sortedKeys != continuousKeys) {
            val newSlots = mutableMapOf<Int, SkillSlot>()
            sortedKeys.forEachIndexed { index, oldKey ->
                val slot = slots[oldKey]!!
                newSlots[index + 1] = slot.copyWithIndex(index + 1)
            }
            slots.clear()
            slots.putAll(newSlots)
        }
    }

    fun toNbt(): NbtCompound = NbtCompound().apply {
        put("skills", NbtList().apply {
            skills.map { (skill, data) ->
                add(data.toNbt().apply {
                    putString("id", skill.id.toString())
                })
            }
        })
        put("slots", slots.values.map { it.toNbt() }.toNbtCompoundList())
    }

    companion object {

        private val LOGGER: Logger = LogUtils.getLogger()
        const val MAX_SLOT_SIZE = 10

        @JvmStatic
        val DEFAULT_SLOTS = mapOf(
            "active" to 3,
            "generic" to 1,
            "passive" to 2,
        )

        fun fromNbt(tag: NbtCompound): SkillContainer {
            val skills = linkedMapOf<Skill, SkillData>()
            if (tag.contains("skills", NbtElement.LIST_TYPE.toInt())) {
                tag.getList("skills", NbtElement.COMPOUND_TYPE.toInt()).forEach {
                    it as NbtCompound
                    val id = it.getString("id")
                    Skills.fromIdNullable(id)?.let { skill ->
                        skills[skill] = SkillData.fromNbt(it)
                    } ?: run {
                        LOGGER.warn("Found invalid skill id $id while reading skills from NBT")
                    }
                }
            }
            val slots = if (tag.contains("slots", NbtElement.LIST_TYPE.toInt())) tag.getList(
                "slots",
                NbtElement.COMPOUND_TYPE.toInt()
            ).filterIsInstance<NbtCompound>()
                .map { SkillSlot.fromNbt(it) }
                .associateBy { it.index }
                .toMutableMap() else createDefaultSlots()
            return SkillContainer(skills, slots)
        }

        fun createDefaultSlots(): MutableMap<Int, SkillSlot> =
            mutableMapOf<Int, SkillSlot>().apply {
                var index = 1
                val slots = SkillConfig.get().defaultSkillSlots
                slots["active"]?.takeIf { it > 0 }?.let {
                    repeat(it) {
                        put(index, SkillSlot.Active(index))
                        index++
                    }
                }
                slots["generic"]?.takeIf { it > 0 }?.let {
                    repeat(it) {
                        put(index, SkillSlot.Generic(index))
                        index++
                    }
                }
                slots["passive"]?.takeIf { it > 0 }?.let {
                    repeat(it) {
                        put(index, SkillSlot.Passive(index))
                        index++
                    }
                }
            }
    }
}