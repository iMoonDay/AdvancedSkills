package com.imoonday.advskills_re.config

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.mojang.logging.*
import dev.architectury.platform.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.slf4j.*
import java.io.*

@Serializable
class GlobalConfig {

    val defaultSkillSlots: MutableMap<String, Int> = SkillContainer.DEFAULT_SLOTS.toMutableMap()
    var disableSkillFruitGeneration: Boolean = false
        set(value) {
            field = value
            save()
        }
    var oakLeavesDropChance: Float = 0.005f
        set(value) {
            field = value
            save()
        }
    var darkOakLeavesDropChance: Float = 0.005f
        set(value) {
            field = value
            save()
        }
    var ancientCityChestGenerationChance: Float = 0.25f
        set(value) {
            field = value
            save()
        }
    var buriedTreasureChestGenerationChance: Float = 0.25f
        set(value) {
            field = value
            save()
        }
    var endCityTreasureChestGenerationChance: Float = 0.25f
        set(value) {
            field = value
            save()
        }
    var spawnBonusChestGenerationChance: Float = 1f
        set(value) {
            field = value
            save()
        }
    private val skillRarityWeights: MutableMap<String, Int> =
        SkillRarity.DEFAULT_WEIGHTS.mapKeys { it.key.id }.toMutableMap()

    fun getDefaultSkillSlots(slot: String): Int = defaultSkillSlots[slot] ?: 0

    fun setDefaultSkillSlot(slot: String, count: Int) {
        defaultSkillSlots[slot] = count
        save()
    }

    fun getRarityWeight(rarity: SkillRarity): Int = skillRarityWeights[rarity.id] ?: 0

    fun setRarityWeight(rarity: SkillRarity, weight: Int) {
        skillRarityWeights[rarity.id] = weight
        save()
    }

    fun toJson(): String = JSON.encodeToString(serializer(), this)

    fun load() {
        LOGGER.info("Loading $MOD_ID-global configuration file")
        try {
            if (!file.exists()) {
                save()
            } else {
                instance = fromJson(file.readText(Charsets.UTF_8))
            }
        } catch (e: Exception) {
            LOGGER.error(
                "Read $MOD_ID-global configuration failed. Try to save the current configuration", e
            )
            save()
        }
    }

    fun save() {
        try {
            file.writeText(instance.toJson(), Charsets.UTF_8)
        } catch (e: Exception) {
            LOGGER.error("Couldn't save $MOD_ID-global configuration file", e)
        }
    }

    companion object {

        private val LOGGER: Logger = LogUtils.getLogger()
        private val JSON = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        private var file: File = Platform.getConfigFolder().resolve("$MOD_ID-global.json").toFile()
        private var instance = GlobalConfig()

        @JvmStatic
        fun get(): GlobalConfig = instance

        @JvmStatic
        fun fromJson(json: String): GlobalConfig = JSON.decodeFromString(serializer(), json)
    }
}
