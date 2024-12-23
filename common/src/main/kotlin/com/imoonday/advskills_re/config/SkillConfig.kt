package com.imoonday.advskills_re.config

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.component.*
import kotlinx.serialization.*
import net.minecraft.nbt.*
import net.minecraft.server.*
import net.minecraft.util.*

@Serializable
class SkillConfig {

    @Transient
    private var saveAction: (() -> Unit)? = null

    var skillCooldownMultiplier: Double? = null
    var skillXpMultiplier: Double? = null
    val skillModifier: MutableMap<String, SkillModifier> = mutableMapOf()
    val skillBlackList: MutableSet<String> = mutableSetOf()

    fun getModifier(id: Identifier): SkillModifier? =
        skillModifier[id.toString()] ?: if (id.namespace == MOD_ID) skillModifier[id.path] else null

    fun getOrCreateModifier(id: Identifier): SkillModifier =
        skillModifier.getOrPut(id.toString()) { SkillModifier.EMPTY }

    fun removeModifier(id: Identifier): Boolean {
        val result1 = skillModifier.remove(id.toString()) != null
        val result2 = if (id.namespace == MOD_ID) skillModifier.remove(id.path) != null else false
        return result1 || result2
    }

    fun isInBlackList(id: Identifier): Boolean =
        skillBlackList.contains(id.toString()) || id.namespace == MOD_ID && skillBlackList.contains(id.path)

    fun addBlackList(id: Identifier) {
        skillBlackList.add(id.toString())
    }

    fun removeBlackList(id: Identifier): Boolean {
        val result1 = skillBlackList.remove(id.toString())
        val result2 = if (id.namespace == MOD_ID) skillBlackList.remove(id.path) else false
        return result1 || result2
    }

    fun load(tag: NbtCompound) {
        if (tag.contains("skillModifier")) {
            skillModifier.clear()

            val skillModifierTag = tag.getCompound("skillModifier")
            for (id in skillModifierTag.keys) {
                val modifierTag = skillModifierTag.getCompound(id)
                skillModifier[id] = SkillModifier.fromNbt(modifierTag)
            }
        }

        if (tag.contains("skillBlackList")) {
            skillBlackList.clear()

            val skillBlackListTag = tag.getList("skillBlackList", NbtElement.STRING_TYPE.toInt())
            skillBlackListTag.forEach {
                skillBlackList.add(it.asString())
            }
        }

        if (tag.contains("skillCooldownMultiplier")) {
            skillCooldownMultiplier = tag.getDouble("skillCooldownMultiplier")
        }

        if (tag.contains("skillXpMultiplier")) {
            skillXpMultiplier = tag.getDouble("skillXpMultiplier")
        }
    }

    fun save(tag: NbtCompound = NbtCompound()): NbtCompound = tag.apply {
        put("skillModifier", NbtCompound().apply {
            for ((id, modifier) in skillModifier) {
                put(id, modifier.toNbt())
            }
        })
        put("skillBlackList", NbtList().apply {
            skillBlackList.forEach { add(NbtString.of(it)) }
        })
        if (skillCooldownMultiplier != null) {
            putDouble("skillCooldownMultiplier", skillCooldownMultiplier!!)
        }
        if (skillXpMultiplier != null) {
            putDouble("skillXpMultiplier", skillXpMultiplier!!)
        }
    }

    fun reset() {
        skillModifier.clear()
        skillBlackList.clear()
        skillCooldownMultiplier = null
        skillXpMultiplier = null
    }

    fun markDirty() {
        saveAction?.invoke()
    }

    fun setSaveAction(action: (() -> Unit)?) {
        saveAction = action
    }

    fun connectToServer(server: MinecraftServer) {
        server.overworld.persistentStateManager
            .getOrCreate(SkillConfigState.Companion::fromNbt, ::SkillConfigState, MOD_ID)
            .run {
                markDirty()
                setSaveAction(::markDirty)
            }
    }

    fun disconnect() = instance.run {
        reset()
        setSaveAction(null)
    }

    fun isConnected(): Boolean = saveAction != null

    companion object {

        private val instance = SkillConfig()

        @JvmStatic
        fun get(): SkillConfig = instance.apply(SkillConfig::markDirty)
    }
}