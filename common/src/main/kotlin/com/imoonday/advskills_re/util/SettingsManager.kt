package com.imoonday.advskills_re.util

import com.google.gson.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.*
import com.mojang.logging.*
import dev.architectury.platform.*
import net.minecraft.sound.*
import net.minecraft.text.*
import net.minecraft.util.*
import java.io.*
import java.nio.file.*
import kotlin.io.path.*

object SettingsManager {

    private val GSON: Gson = GsonBuilder().setPrettyPrinting().setLenient()
        .registerTypeAdapter(Identifier::class.java, Identifier.Serializer())
        .registerTypeAdapter(SoundEvent::class.java, Serializers.SOUND_EVENT)
        .registerTypeHierarchyAdapter(Text::class.java, Serializers.TextSerializer)
        .registerTypeAdapter(Enhancement::class.java, Enhancement.Serializer())
        .registerTypeAdapter(SkillParameter::class.java, SkillParameter.Serializer())
        .registerTypeAdapter(SkillRarity::class.java, SkillRarity.SerializerById)
        .create()
    private val LOGGER = LogUtils.getLogger()
    private val settingsDir = Platform.getConfigFolder().resolve("advskills_re/skills")
    private val settings: MutableMap<Identifier, Skill.Settings> = mutableMapOf()

    @JvmStatic
    fun tryLoad(file: File): Skill.Settings? {
        if (!file.exists()) return null

        return try {
            GSON.fromJson(file.readText(), Skill.Settings::class.java)
        } catch (e: Exception) {
            LOGGER.error("Failed to load Skill Settings from $file", e)
            null
        }
    }

    @JvmStatic
    fun loadFiles() {
        if (!settingsDir.isDirectory()) return

        val loadedCount = settingsDir.listAllFiles(".*\\.json").map { it.toFile() }.count { file ->
            tryLoad(file)?.let {
                if (settings.containsKey(it.id)) {
                    LOGGER.warn("Duplicate Skill Settings found for ${it.id}, skipping")
                    false
                } else {
                    settings[it.id] = it
                    true
                }
            } ?: false
        }

        LOGGER.info("Loaded $loadedCount Skill Settings")
    }

    @JvmStatic
    fun saveSettings(skill: Skill) {
        if (!checkOrCreateDirectory(settingsDir)) return

        val settings = skill.settings

        val id = settings.id
        val namespaceDir = settingsDir.resolve(id.namespace)
        if (!checkOrCreateDirectory(namespaceDir)) return

        val file = namespaceDir.resolve("${id.path}.json")
        try {
            val json = try {
                GSON.toJson(settings)
            } catch (e: Exception) {
                LOGGER.error("Failed to encode Skill Settings: $settings", e)
                return
            }
            file.writeText(json)
        } catch (e: Exception) {
            LOGGER.error("Failed to save Skill Settings for $id", e)
        }
    }

    @JvmStatic
    fun saveAll(skills: Collection<Skill>) {
        if (!checkOrCreateDirectory(settingsDir)) return

        var successCount = 0

        for (skill in skills) {
            val settings = skill.settings

            val id = settings.id
            val namespaceDir = settingsDir.resolve(id.namespace)
            if (!checkOrCreateDirectory(namespaceDir, false)) {
                continue
            }

            val file = namespaceDir.resolve("${id.path}.json")
            try {
                val json = try {
                    GSON.toJson(settings)
                } catch (e: Exception) {
                    LOGGER.error("Failed to encode Skill Settings: $settings", e)
                    continue
                }
                file.writeText(json)
                successCount++
            } catch (ignore: Exception) {
            }
        }

        LOGGER.info("Skill Settings saved: $successCount succeeded, ${skills.size - successCount} failed")
    }

    @JvmStatic
    fun loadOrSaveFiles(skills: Collection<Skill>) {
        loadFiles()
        saveMissing(skills)
    }

    @JvmStatic
    fun saveMissing(skills: Collection<Skill>) = saveAll(skills.filter { it.id !in settings })

    @JvmStatic
    private fun checkOrCreateDirectory(path: Path, error: Boolean = true): Boolean {
        if (!path.isDirectory()) {
            try {
                path.createDirectories()
            } catch (e: Exception) {
                if (error) {
                    LOGGER.error("Failed to create Skill Settings directory", e)
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
}