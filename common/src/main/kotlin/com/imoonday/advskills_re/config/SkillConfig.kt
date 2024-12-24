package com.imoonday.advskills_re.config

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.component.*
import com.mojang.logging.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.minecraft.nbt.*
import net.minecraft.server.*
import net.minecraft.util.*
import org.slf4j.*
import java.io.*
import java.nio.file.*

@Serializable
class SkillConfig {

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

    fun toJson(): String = JSON.encodeToString(serializer(), this)

    fun load() {
        val file = file ?: return

        LOGGER.info("Loading $MOD_ID-server configuration file")
        try {
            if (!file.exists()) {
                save()
            } else {
                instance = fromJson(file.readText(Charsets.UTF_8))
            }
        } catch (e: Exception) {
            LOGGER.error(
                "Read $MOD_ID-server configuration failed. Try to save the current configuration", e
            )
            save()
        }
    }

    fun save() {
        val file = file ?: return

        try {
            file.writeText(instance.toJson(), Charsets.UTF_8)
        } catch (e: Exception) {
            LOGGER.error("Couldn't save $MOD_ID-server configuration file", e)
        }
    }

    fun loadFromNbt(tag: NbtCompound) {
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

    fun writeToNbt(tag: NbtCompound = NbtCompound()): NbtCompound = tag.apply {
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

    companion object {

        private val LOGGER: Logger = LogUtils.getLogger()
        private val JSON = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        private val serverConfig: WorldSavePath = WorldSavePath("serverconfig")
        private var file: File? = null
        private var instance = SkillConfig()

        @JvmStatic
        fun get(): SkillConfig = instance

        @JvmStatic
        fun fromJson(json: String): SkillConfig = JSON.decodeFromString(serializer(), json)

        @JvmStatic
        fun init(server: MinecraftServer) {
            server.overworld.persistentStateManager
                .get(SkillConfigState.Companion::fromNbt, MOD_ID)
                ?.let { instance.loadFromNbt(it.config.writeToNbt()) }

            file = getServerConfigPath(server).resolve("$MOD_ID-server.json").toFile()

            instance.load()
        }

        @JvmStatic
        fun resetFile() {
            file = null
        }

        private fun getServerConfigPath(server: MinecraftServer): Path {
            val serverConfig = server.getSavePath(serverConfig)
            if (!Files.isDirectory(serverConfig)) {
                try {
                    Files.createDirectories(serverConfig)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            return serverConfig
        }
    }
}