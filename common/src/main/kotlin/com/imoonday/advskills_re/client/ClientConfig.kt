package com.imoonday.advskills_re.client

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.enums.*
import com.mojang.logging.*
import dev.architectury.platform.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.slf4j.*
import java.io.*

@Serializable
class ClientConfig {

    var uiOffsetX: Int = 0
    var uiOffsetY: Int = 0
    var skillSorter: SkillSorter = SkillSorter.DEFAULT
    var layout: Array<IntArray> = DEFAULT_LAYOUT
        get() {
            if (!isValidLayout(field)) field = DEFAULT_LAYOUT
            return field
        }
        set(value) {
            if (isValidLayout(value)) field = value else return
        }
    var quickCastWheelHoldTime: Int = 250
    var hideSkillCrosshair: Boolean = false
    var hideSkillInfo: Boolean = false
    var hideSkillSlots: HideMode = HideMode.DYNAMICALLY_HIDE
    var dynamicallyHideDirection: AnimationDirection = AnimationDirection.RIGHT
    var progressBarColor: Int = 0xFFFFEE58.toInt()
    var displaySelectedSkillSlot: Boolean = true
    var displayQuickCastKey: Boolean = true
    var useVanillaSlot: Boolean = false
    var selectedSlotPosition: SlotPosition = SlotPosition.LEFT_OF_HOTBAR
    var selectedSlotOffsetX: Int = 0
    var selectedSlotOffsetY: Int = 0
    var displayProgressBarBelowCrosshair: Boolean = true
    var progressBarOffsetY: Int = 0
    var developmentMode: Boolean = false
    var useRingCastingWheel: Boolean = true
    var disableLearningNotifications: Boolean = false
    var displayedSkills: MutableSet<String> = mutableSetOf()
    var topSkills: MutableList<String> = mutableListOf()

    fun toJson(): String = JSON.encodeToString(serializer(), this)

    fun load() {
        LOGGER.info("Loading $MOD_ID-client configuration file")
        try {
            if (file.exists()) {
                instance = fromJson(file.readText(Charsets.UTF_8))
            } else {
                var renamed = false
                for (oldFile in oldFiles) {
                    if (oldFile.exists()) {
                        try {
                            instance = fromJson(oldFile.readText(Charsets.UTF_8))
                            oldFile.renameTo(file)
                            renamed = true
                            break
                        } catch (e: Exception) {
                            continue
                        }
                    }
                }
                if (!renamed) {
                    save()
                }
            }
        } catch (e: Exception) {
            LOGGER.error(
                "Read $MOD_ID-client configuration failed. Try to save the current configuration", e
            )
            save()
        }
    }

    fun getLayoutOfStringList(): List<String> = layout.map { row ->
        row.joinToString(separator = ",", transform = Int::toString)
    }

    fun setLayoutFromStringList(layoutStringList: List<String>) {
        val newLayout = try {
            layoutStringList.map { row ->
                row.replace(" ", "").split(",").map { it.toInt() }.toIntArray()
            }.toTypedArray()
        } catch (e: Exception) {
            LOGGER.error("Invalid layout string list: $layoutStringList", e)
            return
        }
        if (isValidLayout(newLayout)) {
            layout = newLayout
        } else {
            LOGGER.error("Invalid layout: $newLayout")
        }
    }

    fun isTopSkill(skill: Skill): Boolean = topSkills.contains(skill.id.toString())

    fun save() {
        try {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }

            file.writeText(instance.toJson(), Charsets.UTF_8)
        } catch (e: Exception) {
            LOGGER.error("Couldn't save $MOD_ID-client configuration file", e)
        }
    }

    companion object {

        private val LOGGER: Logger = LogUtils.getLogger()
        private val JSON = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        private var oldFiles: Array<File> = arrayOf(
            Platform.getConfigFolder().resolve("$MOD_ID.json").toFile(),
            Platform.getConfigFolder().resolve("$MOD_ID-client.json").toFile()
        )
        private var file: File = Platform.getConfigFolder().resolve("advskills_re/client.json").toFile()
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
        val DEFAULT_LAYOUT_STRING_LIST = DEFAULT_LAYOUT.map { row ->
            row.joinToString(separator = ",", transform = Int::toString)
        }

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

        @JvmStatic
        fun isValidStringLayout(str: String): Boolean = str.replace(" ", "").split(",").all {
            val value = it.toIntOrNull()
            value != null && value in 0..10
        }

        @JvmStatic
        fun findMissingNumbersFromStringList(layoutStringList: List<String>): List<Int> {
            val numbers = mutableSetOf<Int>()
            for (row in layoutStringList) {
                for (num in row.replace(" ", "").split(",")) {
                    val value = num.toIntOrNull()
                    if (value != null) {
                        numbers.add(value)
                    }
                }
            }
            return (1..10).filter { it !in numbers }.toList()
        }

        @JvmStatic
        fun findRedundantNumbersFromStringList(layoutStringList: List<String>): List<Int> {
            val numbers = mutableMapOf<Int, Int>()
            for (row in layoutStringList) {
                for (num in row.replace(" ", "").split(",")) {
                    val value = num.toIntOrNull()
                    if (value != null && value != 0) {
                        numbers[value] = numbers.getOrDefault(value, 0) + 1
                    }
                }
            }
            return numbers.filter { it.value > 1 }.map { it.key }.toList()
        }
    }
}
