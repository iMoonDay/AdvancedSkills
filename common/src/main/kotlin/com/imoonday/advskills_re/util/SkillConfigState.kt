package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.config.*
import net.minecraft.nbt.*
import net.minecraft.world.*

class SkillConfigState : PersistentState() {

    val config: SkillConfig
        get() = SkillConfig.get()

    override fun writeNbt(nbt: NbtCompound): NbtCompound = config.save(nbt)

    companion object {

        fun fromNbt(nbt: NbtCompound): SkillConfigState = SkillConfigState().apply { config.load(nbt) }
    }
}