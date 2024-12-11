package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enhancement.*
import com.imoonday.advskills_re.util.*
import net.minecraft.nbt.*
import net.minecraft.text.*
import net.minecraft.util.*

class SkillEnhancementType<T : SkillEnhancement>(
    val id: String,
    val name: Text,
    val description: Text,
    val maxLevel: Int,
    private val factory: Factory<T>
) {

    init {
        check(maxLevel > 0) { "Max level must be greater than 0" }
    }

    fun create(level: Int = 1): T = factory.create(this, level)

    fun createMax(): T = factory.create(this, maxLevel)

    fun createRandom(): T = factory.create(this, getRandomLevel())

    private fun getRandomLevel(): Int =
        if (maxLevel == 1) 1 else (1..maxLevel).toList().randomByWeight({ maxLevel - it + 1 }, 1)

    fun interface Factory<T : SkillEnhancement> {

        fun create(type: SkillEnhancementType<T>, level: Int): T
    }

    companion object {

        @JvmStatic
        fun create(nbt: NbtCompound): SkillEnhancement = by(nbt).create(1).apply { load(nbt) }

        @JvmStatic
        fun createNullable(nbt: NbtCompound): SkillEnhancement? = byNullable(nbt)?.create(1)?.apply { load(nbt) }

        @JvmStatic
        fun by(nbt: NbtCompound): SkillEnhancementType<*> = byNullable(nbt) ?: SkillEnhancements.EMPTY

        @JvmStatic
        fun byNullable(nbt: NbtCompound): SkillEnhancementType<*>? {
            var id = nbt.getString("id")
            val identifier = Identifier.tryParse(id)
            if (identifier != null) {
                id = identifier.path
            }
            return SkillEnhancements.get(id)
        }
    }
}