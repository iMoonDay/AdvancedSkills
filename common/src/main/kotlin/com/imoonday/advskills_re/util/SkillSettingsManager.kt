package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.skill.*
import com.mojang.logging.*
import dev.architectury.platform.*
import kotlinx.serialization.json.*
import net.minecraft.util.*
import java.io.*
import kotlin.io.path.*

object SkillSettingsManager {

    private val LOGGER = LogUtils.getLogger()
    private val settingsDir = Platform.getConfigFolder().resolve("advskills_re/skills")
    private val settings: MutableMap<Identifier, Skill.Settings> = mutableMapOf()

    fun tryLoad(file: File): Skill.Settings? {
        if (!file.exists()) return null

        try {
            val skillSettings = Json.decodeFromString(Skill.Settings.serializer(), file.readText())
            settings[skillSettings.id] = skillSettings
            return skillSettings
        } catch (e: Exception) {
            return null
        }
    }

    fun loadFiles() {
        if (!settingsDir.isDirectory()) return

        val loadedCount = settingsDir.listDirectoryEntries("*.json").map { it.toFile() }.count { file ->
            tryLoad(file)?.let {
                if (settings.containsKey(it.id)) {
                    LOGGER.warn("Duplicate skill settings found for ${it.id}, skipping.")
                    false
                } else {
                    settings[it.id] = it
                    true
                }
            } ?: false
        }

        LOGGER.info("Loaded $loadedCount skill settings.")
    }

    fun saveSettings(skill: Skill) {
        if (!settingsDir.isDirectory()) {
            try {
                settingsDir.createDirectories()
            } catch (e: Exception) {
                LOGGER.error("Failed to create skill settings directory.", e)
                return
            }
        }

        val settings = Skill.Settings.from(skill)
        val file = settingsDir.resolve("${settings.id.toTranslationKey()}.json")
        try {
            file.writeText(Json.encodeToString(Skill.Settings.serializer(), settings))
        } catch (e: Exception) {
            LOGGER.error("Failed to save skill settings for ${settings.id}.", e)
        }
    }

    fun saveAll(skills: Collection<Skill>) {
        if (!settingsDir.isDirectory()) {
            try {
                settingsDir.createDirectories()
            } catch (e: Exception) {
                LOGGER.error("Failed to create skill settings directory.", e)
                return
            }
        }

        var successCount = 0
        var failureCount = 0

        skills.forEach {
            val settings = Skill.Settings.from(it)
            val file = settingsDir.resolve("${settings.id.toTranslationKey()}.json")
            try {
                file.writeText(Json.encodeToString(Skill.Settings.serializer(), settings))
                successCount++
            } catch (e: Exception) {
                failureCount++
            }
        }

        LOGGER.info("Skill settings saved: $successCount succeeded, $failureCount failed.")
    }

    fun saveMissing(skills: Collection<Skill>) = saveAll(skills.filter { it.id !in settings })

    fun getSettings(id: Identifier): Skill.Settings? = settings[id]

    fun getSettings(skill: Skill): Skill.Settings? = getSettings(skill.id)
}