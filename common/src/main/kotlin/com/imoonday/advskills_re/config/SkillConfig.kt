package com.imoonday.advskills_re.config

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.util.*
import com.mojang.logging.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.minecraft.nbt.*
import net.minecraft.server.*
import net.minecraft.util.*
import org.slf4j.*
import java.io.*

@Serializable
class SkillConfig {

    var skillCooldownMultiplier: Double? = null
    var skillXpMultiplier: Double? = null
    val skillBlackList: MutableSet<String> = mutableSetOf()

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
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }

            file.writeText(instance.toJson(), Charsets.UTF_8)
        } catch (e: Exception) {
            LOGGER.error("Couldn't save $MOD_ID-server configuration file", e)
        }
    }

    fun loadFromNbt(tag: NbtCompound) {
        if (tag.contains("skillBlackList")) {
            skillBlackList.clear()

            val listTag = tag.getList("skillBlackList", NbtElement.STRING_TYPE.toInt())
            skillBlackList.addAll(listTag.toStringList())
        }

        if (tag.contains("skillCooldownMultiplier")) {
            skillCooldownMultiplier = tag.getDouble("skillCooldownMultiplier")
        }

        if (tag.contains("skillXpMultiplier")) {
            skillXpMultiplier = tag.getDouble("skillXpMultiplier")
        }
    }

    fun writeToNbt(tag: NbtCompound = NbtCompound()): NbtCompound = tag.apply {
        put("skillBlackList", skillBlackList.toNbtStringList())
        if (skillCooldownMultiplier != null) {
            putDouble("skillCooldownMultiplier", skillCooldownMultiplier!!)
        }
        if (skillXpMultiplier != null) {
            putDouble("skillXpMultiplier", skillXpMultiplier!!)
        }
    }

    fun reset() {
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

            file = server.serverConfigPath.resolve("advskills_re/server.json").toFile()

            instance.load()
        }

        @JvmStatic
        fun resetFile() {
            file = null
        }
    }
}