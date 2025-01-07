package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.skill.*
import com.mojang.logging.*
import dev.architectury.platform.*
import net.minecraft.server.*
import net.minecraft.util.*
import java.io.*
import java.nio.file.*
import kotlin.io.path.*

object SettingsManager {

    private val LOGGER = LogUtils.getLogger()
    private const val VERSION = 1
    private const val BACKUP_SUFFIX = ".bak"
    private val settingsDir = Platform.getConfigFolder().resolve("advskills_re/skills")
    private val settings: MutableMap<Identifier, Skill.Settings> = mutableMapOf()

    @JvmStatic
    fun tryLoad(file: File): Skill.Settings? =
        if (!file.exists()) null else Skill.Settings.fromJsonWithVersion(file.readText(), VERSION)

    @JvmStatic
    fun loadFiles() {
        if (!settingsDir.isDirectory()) return

        settings.clear()

        val loadedCount = settingsDir.listAllFiles(".*\\.json").map { it.toFile() }.count { file ->
            tryLoad(file)?.let {
                if (settings.containsKey(it.id)) {
                    LOGGER.warn("Duplicate skill settings found for ${it.id}, skipping")
                    false
                } else {
                    settings[it.id] = it
                    true
                }
            } ?: false
        }

        LOGGER.info("Loaded $loadedCount skill settings")
    }

    @JvmStatic
    fun saveSettings(skill: Skill) {
        if (!checkOrCreateDirectory(settingsDir)) return

        val settings = skill.settings
        val id = settings.id
        val namespaceDir = getNamespaceDir(id)
        if (!checkOrCreateDirectory(namespaceDir)) return

        val file = namespaceDir.resolve("${id.path}.json")
        try {
            val json = try {
                settings.toJsonWithVersion(VERSION)
            } catch (e: Exception) {
                LOGGER.error("Failed to encode skill settings: $settings", e)
                return
            }

            if (file.exists()) {
                tryBackupFile(file)
            }

            file.writeText(json)
        } catch (e: Exception) {
            LOGGER.error("Failed to save skill settings for $id", e)
        }
    }

    @JvmStatic
    fun saveAll(skills: Collection<Skill>) {
        if (!checkOrCreateDirectory(settingsDir)) return
        if (skills.isEmpty()) return

        var successCount = 0

        for (skill in skills) {
            val settings = skill.settings
            val id = settings.id
            val namespaceDir = getNamespaceDir(id)
            if (!checkOrCreateDirectory(namespaceDir, false)) {
                continue
            }

            val file = namespaceDir.resolve("${id.path}.json")
            try {
                val json = try {
                    settings.toJsonWithVersion(VERSION)
                } catch (e: Exception) {
                    LOGGER.error("Failed to encode skill settings: $settings", e)
                    continue
                }

                if (file.exists()) {
                    tryBackupFile(file)
                }

                file.writeText(json)
                successCount++
            } catch (ignore: Exception) {
            }
        }

        LOGGER.info("Skill settings saved: $successCount succeeded, ${skills.size - successCount} failed")
    }

    private fun getNamespaceDir(id: Identifier): Path =
        if (id.namespace == MOD_ID) settingsDir else settingsDir.resolve(id.namespace)

    @JvmStatic
    fun loadOrSaveFiles(skills: Collection<Skill>) {
        loadFiles()
        saveMissing(skills)
    }

    @JvmStatic
    fun loadFromServerConfig(server: MinecraftServer): Map<Identifier, Skill.Settings> = buildMap {
        val serverConfigPath = server.serverConfigPath
        if (serverConfigPath.isDirectory()) {
            val skillsDir = serverConfigPath.resolve("advskills_re/skills")
            if (!skillsDir.isDirectory()) {
                runCatching {
                    skillsDir.createDirectories()
                }.onFailure {
                    LOGGER.error("Failed to create skills directory in serverconfig", it)
                }
            } else {
                skillsDir.listAllFiles(".*\\.json").map { it.toFile() }.count { file ->
                    tryLoad(file)?.also { put(it.id, it) } != null
                }.also {
                    if (it > 0) {
                        LOGGER.info("Loaded $it skill settings from server config")
                    }
                }
            }
        }
    }

    @JvmStatic
    fun saveMissing(skills: Collection<Skill>) = saveAll(skills.filter { it.id !in settings })

    private fun checkOrCreateDirectory(path: Path, error: Boolean = true): Boolean {
        if (!path.isDirectory()) {
            try {
                path.createDirectories()
            } catch (e: Exception) {
                if (error) {
                    LOGGER.error("Failed to create skill settings directory", e)
                }
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun getSettings(id: Identifier): Skill.Settings? = settings[id]

    @JvmStatic
    fun getSettings(skill: Skill): Skill.Settings? = getSettings(skill.id)

    private fun tryBackupFile(file: Path) {
        try {
            val backupFile = file.resolveSibling(file.name + BACKUP_SUFFIX)
            Files.copy(file, backupFile, StandardCopyOption.REPLACE_EXISTING)
            LOGGER.info("Created backup of settings file: ${backupFile.fileName}")
        } catch (e: Exception) {
            LOGGER.error("Failed to create backup for file: ${file.fileName}", e)
        }
    }
}