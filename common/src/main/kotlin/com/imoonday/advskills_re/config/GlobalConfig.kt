package com.imoonday.advskills_re.config

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.mojang.logging.*
import dev.architectury.platform.*
import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.minecraft.nbt.*
import org.slf4j.*
import java.io.*

@Serializable
class GlobalConfig {

    @Transient
    private var loading: Boolean = false

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
    val skillConfig: SkillConfig = SkillConfig().apply {
        this.skillCooldownMultiplier = 1.0
        this.skillXpMultiplier = 1.0
        this.skillModifier["$MOD_ID:example ($MOD_ID can ignore, fields of modifier can ignore too)"] =
            SkillModifier(20, SkillRarity.EPIC, 100)
        this.skillBlackList += "$MOD_ID:example ($MOD_ID can ignore)"
        this.initSkillConfig()
    }

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

    private fun SkillConfig.initSkillConfig() {
        this.setSaveAction { this@GlobalConfig.save() }
    }

    fun toJson(): String = JSON.encodeToString(serializer(), this)

    fun load() {
        LOGGER.info("Loading $MOD_ID-global configuration file")
        try {
            if (!file.exists()) {
                save()
            } else {
                instance = fromJson(file.readText(Charsets.UTF_8)).apply {
                    this.skillConfig.initSkillConfig()
                }
            }
        } catch (e: Exception) {
            LOGGER.error(
                "Read $MOD_ID-global configuration failed. Try to save the current configuration", e
            )
            save()
        }
    }

    fun save() {
        if (loading) return
        try {
            file.writeText(instance.toJson(), Charsets.UTF_8)
        } catch (e: Exception) {
            LOGGER.error("Couldn't save $MOD_ID-global configuration file", e)
        }
    }

    fun toNbt(): NbtCompound = NbtCompound().apply {
        put("defaultSkillSlots", NbtCompound().apply {
            defaultSkillSlots.forEach { (k, v) -> putInt(k, v) }
        })
        putBoolean("disableSkillFruitGeneration", disableSkillFruitGeneration)
        putFloat("oakLeavesDropChance", oakLeavesDropChance)
        putFloat("darkOakLeavesDropChance", darkOakLeavesDropChance)
        putFloat("ancientCityChestGenerationChance", ancientCityChestGenerationChance)
        putFloat("buriedTreasureChestGenerationChance", buriedTreasureChestGenerationChance)
        putFloat("endCityTreasureChestGenerationChance", endCityTreasureChestGenerationChance)
        putFloat("spawnBonusChestGenerationChance", spawnBonusChestGenerationChance)
        put("skillRarityWeights", NbtCompound().apply {
            skillRarityWeights.forEach { (k, v) -> putInt(k, v) }
        })
        put("skillConfig", skillConfig.save())
    }

    fun getSkillConfigNbt(): NbtCompound = NbtCompound().apply {
        put("skillConfig", skillConfig.save())
    }

    fun loadFromNbt(nbt: NbtCompound) {
        loading = true

        if (nbt.contains("defaultSkillSlots")) {
            defaultSkillSlots.clear()
            val defaultSkillSlotsNbt = nbt.getCompound("defaultSkillSlots")
            defaultSkillSlotsNbt.keys.forEach {
                defaultSkillSlots[it] = defaultSkillSlotsNbt.getInt(it)
            }
        }
        if (nbt.contains("disableSkillFruitGeneration")) {
            disableSkillFruitGeneration = nbt.getBoolean("disableSkillFruitGeneration")
        }
        if (nbt.contains("oakLeavesDropChance")) {
            oakLeavesDropChance = nbt.getFloat("oakLeavesDropChance")
        }
        if (nbt.contains("darkOakLeavesDropChance")) {
            darkOakLeavesDropChance = nbt.getFloat("darkOakLeavesDropChance")
        }
        if (nbt.contains("ancientCityChestGenerationChance")) {
            ancientCityChestGenerationChance = nbt.getFloat("ancientCityChestGenerationChance")
        }
        if (nbt.contains("buriedTreasureChestGenerationChance")) {
            buriedTreasureChestGenerationChance = nbt.getFloat("buriedTreasureChestGenerationChance")
        }
        if (nbt.contains("endCityTreasureChestGenerationChance")) {
            endCityTreasureChestGenerationChance = nbt.getFloat("endCityTreasureChestGenerationChance")
        }
        if (nbt.contains("spawnBonusChestGenerationChance")) {
            spawnBonusChestGenerationChance = nbt.getFloat("spawnBonusChestGenerationChance")
        }
        if (nbt.contains("skillRarityWeights")) {
            skillRarityWeights.clear()
            val skillRarityWeightsNbt = nbt.getCompound("skillRarityWeights")
            skillRarityWeightsNbt.keys.forEach {
                skillRarityWeights[it] = skillRarityWeightsNbt.getInt(it)
            }
        }
        if (nbt.contains("skillConfig")) {
            skillConfig.load(nbt.getCompound("skillConfig"))
        }

        loading = false
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
