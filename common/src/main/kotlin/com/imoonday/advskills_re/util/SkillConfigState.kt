package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.config.*
import net.minecraft.nbt.*
import net.minecraft.server.*
import net.minecraft.world.*

class SkillConfigState : PersistentState() {

    val config: SkillConfig = SkillConfig()

    override fun writeNbt(nbt: NbtCompound): NbtCompound = config.toTag(nbt)

    fun syncToGlobalConfig() {
        SkillConfig.instance = config
    }

    companion object {

        fun fromNbt(nbt: NbtCompound): SkillConfigState = SkillConfigState().apply { config.fromTag(nbt) }

        fun fromServer(server: MinecraftServer): SkillConfigState =
            server.overworld.persistentStateManager.getOrCreate(::fromNbt, ::SkillConfigState, MOD_ID)
                .also {
                    it.markDirty()
                    it.syncToGlobalConfig()
                }
    }
}

val MinecraftServer.skillConfig: SkillConfig
    get() = SkillConfigState.fromServer(this).config

val World.skillConfig: SkillConfig
    get() = this.server?.skillConfig ?: SkillConfig.instance