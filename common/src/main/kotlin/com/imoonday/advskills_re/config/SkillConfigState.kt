package com.imoonday.advskills_re.config

import net.minecraft.nbt.*
import net.minecraft.world.*

class SkillConfigState : PersistentState() {

    var config: SkillConfig = SkillConfig()

    override fun writeNbt(nbt: NbtCompound): NbtCompound = nbt

    companion object {

        fun fromNbt(nbt: NbtCompound): SkillConfigState = SkillConfigState().apply { config.loadFromNbt(nbt) }
    }
}