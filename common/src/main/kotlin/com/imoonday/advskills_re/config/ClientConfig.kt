package com.imoonday.advskills_re.config

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.util.*
import com.mojang.logging.*
import dev.architectury.platform.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.slf4j.*
import java.io.*

@Serializable
class ClientConfig {

    var uiOffsetX: Int = 0
        set(value) {
            field = value
            save()
        }
    var uiOffsetY: Int = 0
        set(value) {
            field = value
            save()
        }
    var skillSorter: SkillSorter = SkillSorter.DEFAULT
        set(value) {
            field = value
            save()
        }
    var layout: Array<IntArray> = DEFAULT_LAYOUT
        get() {
            if (!isValidLayout(field)) field = DEFAULT_LAYOUT
            return field
        }
        set(value) {
            if (isValidLayout(value)) field = value else return
            save()
        }
    var quickCastWheelHoldTime: Int = 250
        set(value) {
            field = value
            save()
        }
    var displayedSkills: MutableSet<String> = mutableSetOf()
        set(value) {
            field = value
            save()
        }

    fun toJson(): String = JSON.encodeToString(serializer(), this)

    fun load() {
        LOGGER.info("Loading $MOD_ID configuration file")
        try {
            if (!file.exists()) {
                save()
            } else {
                instance = fromJson(file.readText(Charsets.UTF_8))
            }
        } catch (e: Exception) {
            LOGGER.error(
                "Read $MOD_ID configuration failed. Try to save the current configuration", e
            )
            save()
        }
    }

    fun save() {
        try {
            file.writeText(instance.toJson(), Charsets.UTF_8)
        } catch (e: Exception) {
            LOGGER.error("Couldn't save $MOD_ID configuration file", e)
        }
    }

    companion object {

        private val LOGGER: Logger = LogUtils.getLogger()
        private val JSON = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        private var file: File = Platform.getConfigFolder().resolve("$MOD_ID.json").toFile()
        private var instance = ClientConfig()

        @JvmStatic
        fun get(): ClientConfig = instance

        @JvmStatic
        val DEFAULT_LAYOUT = arrayOf(
            intArrayOf(1, 2),
            intArrayOf(3, 4),
            intArrayOf(5, 6),
            intArrayOf(7, 8),
            intArrayOf(9, 10),
        )

        @JvmStatic
        fun fromJson(json: String): ClientConfig = JSON.decodeFromString(serializer(), json)

        @JvmStatic
        fun isValidLayout(layout: Array<IntArray>?): Boolean {
            if (layout == null) return false
            val numbers = mutableSetOf<Int>()
            for (row in layout) {
                for (num in row) {
                    if (num < 0 || num > 10) {
                        return false
                    }
                    if (num != 0) {
                        numbers.add(num)
                    }
                }
            }
            return numbers.size == 10
        }
    }
}
