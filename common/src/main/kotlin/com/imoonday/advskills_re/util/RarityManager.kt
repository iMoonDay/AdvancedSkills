package com.imoonday.advskills_re.util

import com.google.gson.*
import com.imoonday.advskills_re.component.*
import com.mojang.logging.*
import dev.architectury.platform.*
import net.minecraft.server.*
import net.minecraft.text.*
import java.io.*
import java.nio.file.*
import kotlin.io.path.*

object RarityManager {

    private val GSON: Gson = GsonBuilder().setPrettyPrinting().setLenient()
        .registerTypeHierarchyAdapter(Text::class.java, Serializers.TextSerializer)
        .registerTypeAdapter(SkillRarity::class.java, SkillRarity.Serializer)
        .create()
    private val LOGGER = LogUtils.getLogger()
    private val raritiesDir = Platform.getConfigFolder().resolve("advskills_re/rarities")
    private val rarities: MutableMap<String, SkillRarity> = mutableMapOf()

    @JvmStatic
    fun tryLoad(file: File): SkillRarity? {
        if (!file.exists()) return null

        return try {
            GSON.fromJson(file.readText(), SkillRarity::class.java)
        } catch (e: Exception) {
            LOGGER.error("Failed to load skill rarity from file: $file", e)
            null
        }
    }

    @JvmStatic
    fun loadFiles() {
        if (!raritiesDir.isDirectory()) return

        rarities.clear()

        val loadedCount = raritiesDir.listAllFiles(".*\\.json").map { it.toFile() }.count { file ->
            tryLoad(file)?.let {
                if (rarities.containsKey(it.id)) {
                    LOGGER.warn("Duplicate skill rarity found with id ${it.id}, skipping")
                    false
                } else {
                    rarities[it.id] = it
                    true
                }
            } ?: false
        }

        LOGGER.info("Loaded $loadedCount skill rarities")
    }

    @JvmStatic
    fun loadFromServerConfig(server: MinecraftServer): Map<String, SkillRarity> = buildMap {
        val serverConfigPath = server.serverConfigPath
        if (serverConfigPath.isDirectory()) {
            val skillsDir = serverConfigPath.resolve("advskills_re/rarities")
            if (!skillsDir.isDirectory()) {
                runCatching {
                    skillsDir.createDirectories()
                }.onFailure {
                    LOGGER.error("Failed to create rarities directory in serverconfig", it)
                }
            } else {
                skillsDir.listAllFiles(".*\\.json").map { it.toFile() }.count { file ->
                    tryLoad(file)?.also { put(it.id, it) } != null
                }.also {
                    if (it > 0) {
                        LOGGER.info("Loaded $it skill rarities from server config")
                    }
                }
            }
        }
    }

    @JvmStatic
    fun saveRarity(rarity: SkillRarity) {
        if (!checkOrCreateDirectory(raritiesDir)) return

        val id = rarity.id
        val file = raritiesDir.resolve("$id.json")
        try {
            val json = try {
                GSON.toJson(rarity)
            } catch (e: Exception) {
                LOGGER.error("Failed to encode skill rarity: $rarity", e)
                return
            }
            file.writeText(json)
            LOGGER.info("Saved skill rarity with id $id")
        } catch (e: Exception) {
            LOGGER.error("Failed to save skill rarity with id $id", e)
        }
    }

    @JvmStatic
    fun saveAll(rarities: Collection<SkillRarity>) {
        if (!checkOrCreateDirectory(raritiesDir)) return
        if (rarities.isEmpty()) return

        var successCount = 0

        val savedIds = mutableSetOf<String>()
        for (rarity in rarities) {
            var id = rarity.id
            if (id in savedIds) {
                var i = 1
                while ("$id($i)" in savedIds) {
                    i++
                }
                id = "$id($i)"
            }
            val file = raritiesDir.resolve("$id.json")
            try {
                val json = try {
                    GSON.toJson(rarity)
                } catch (e: Exception) {
                    LOGGER.error("Failed to encode skill rarity: $rarity", e)
                    continue
                }
                file.writeText(json)
                savedIds += id
                successCount++
            } catch (ignore: Exception) {
            }
        }

        LOGGER.info("Skill rarities saved: $successCount succeeded, ${rarities.size - successCount} failed")
    }

    @JvmStatic
    fun saveMissing(rarities: Collection<SkillRarity>) = saveAll(rarities.filter { it.id !in this.rarities })

    private fun checkOrCreateDirectory(path: Path, error: Boolean = true): Boolean {
        if (!path.isDirectory()) {
            try {
                path.createDirectories()
            } catch (e: Exception) {
                if (error) {
                    LOGGER.error("Failed to create skill rarity directory", e)
                }
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun getRarity(id: String): SkillRarity? = rarities[id]

    @JvmStatic
    fun getRarity(level: Int): SkillRarity? = rarities.values.find { it.level == level }

    @JvmStatic
    fun getLoadedRarities(): List<SkillRarity> = rarities.values.toList()
}