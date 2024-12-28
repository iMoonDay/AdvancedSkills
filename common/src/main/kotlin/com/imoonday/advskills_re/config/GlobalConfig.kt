package com.imoonday.advskills_re.config

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
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
            if (!loading) save()
        }
    var oakLeavesDropChance: Float = 0.005f
        set(value) {
            field = value
            if (!loading) save()
        }
    var darkOakLeavesDropChance: Float = 0.005f
        set(value) {
            field = value
            if (!loading) save()
        }
    var ancientCityChestGenerationChance: Float = 0.25f
        set(value) {
            field = value
            if (!loading) save()
        }
    var buriedTreasureChestGenerationChance: Float = 0.25f
        set(value) {
            field = value
            if (!loading) save()
        }
    var endCityTreasureChestGenerationChance: Float = 0.25f
        set(value) {
            field = value
            if (!loading) save()
        }
    var spawnBonusChestGenerationChance: Float = 1f
        set(value) {
            field = value
            if (!loading) save()
        }
    val skillConfig: SkillConfig = SkillConfig().apply {
        this.skillCooldownMultiplier = 1.0
        this.skillXpMultiplier = 1.0
        this.skillBlackList += "$MOD_ID:example ($MOD_ID can ignore)"
    }
    val learningBlacklist: MutableSet<String> = mutableSetOf()

    fun getDefaultSkillSlots(slot: String): Int = defaultSkillSlots[slot] ?: 0

    fun setDefaultSkillSlot(slot: String, count: Int) {
        defaultSkillSlots[slot] = count
        save()
    }

    fun getLearningFilter(): (Skill) -> Boolean =
        { learningBlacklist.isEmpty() || it.id.toString() !in learningBlacklist }

    fun toJson(): String = JSON.encodeToString(serializer(), this)

    fun load() {
        LOGGER.info("Loading $MOD_ID-common configuration file")
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
                "Read $MOD_ID-common configuration failed. Try to save the current configuration", e
            )
            save()
        }
    }

    fun save() {
        try {
            file.writeText(instance.toJson(), Charsets.UTF_8)
        } catch (e: Exception) {
            LOGGER.error("Couldn't save $MOD_ID-common configuration file", e)
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
        put("skillConfig", skillConfig.writeToNbt())
        put("learningBlacklist", learningBlacklist.toNbtStringList())
    }

    fun getSkillConfigNbt(): NbtCompound = NbtCompound().apply {
        put("skillConfig", skillConfig.writeToNbt())
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
        if (nbt.contains("skillConfig")) {
            skillConfig.loadFromNbt(nbt.getCompound("skillConfig"))
        }
        if (nbt.contains("learningBlacklist")) {
            learningBlacklist.clear()
            learningBlacklist.addAll(nbt.getList("learningBlacklist", NbtElement.STRING_TYPE.toInt()).toStringList())
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
        private var oldFiles: Array<File> = arrayOf(
            Platform.getConfigFolder().resolve("$MOD_ID-global.json").toFile(),
            Platform.getConfigFolder().resolve("$MOD_ID-common.json").toFile()
        )
        private var file: File = Platform.getConfigFolder().resolve("advskills_re/common.json").toFile()
        private var instance = GlobalConfig()

        @JvmStatic
        fun get(): GlobalConfig = instance

        @JvmStatic
        fun fromJson(json: String): GlobalConfig = JSON.decodeFromString(serializer(), json)
    }
}
