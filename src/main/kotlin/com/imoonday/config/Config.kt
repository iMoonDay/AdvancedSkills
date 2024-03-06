package com.imoonday.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.imoonday.MOD_ID
import com.imoonday.skill.Skill
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class Config {

    var cooldownModifier: MutableMap<String, MutableMap<String, Int>> = mutableMapOf(
        MOD_ID to mutableMapOf(
            "example_20_ticks" to 20
        )
    )
    var rarityModifier: MutableMap<String, MutableMap<String, Skill.Rarity>> = mutableMapOf(
        MOD_ID to mutableMapOf(
            "example_useless" to Skill.Rarity.USELESS,
            "example_common" to Skill.Rarity.COMMON,
            "example_uncommon" to Skill.Rarity.UNCOMMON,
            "example_rare" to Skill.Rarity.RARE,
            "example_superb" to Skill.Rarity.SUPERB,
            "example_epic" to Skill.Rarity.EPIC,
            "example_legendary" to Skill.Rarity.LEGENDARY,
            "example_mythic" to Skill.Rarity.MYTHIC,
            "example_unique" to Skill.Rarity.UNIQUE
        )
    )
    var skillBlackList: MutableMap<String, MutableList<String>> = mutableMapOf(
        MOD_ID to mutableListOf(
            "example"
        )
    )

    fun toJson(): String = GSON.toJson(this)

    fun fromTag(tag: NbtCompound) {
        cooldownModifier.clear()
        rarityModifier.clear()
        skillBlackList.clear()

        val cooldownModifierTag = tag.getCompound("cooldownModifier")
        for (namespace in cooldownModifierTag.keys) {
            val mapTag = cooldownModifierTag.getCompound(namespace)
            val map = mutableMapOf<String, Int>()
            for (path in mapTag.keys) {
                map[path] = mapTag.getInt(path)
            }
            cooldownModifier[namespace] = map
        }

        val rarityModifierTag = tag.getCompound("rarityModifier")
        for (namespace in rarityModifierTag.keys) {
            val mapTag = rarityModifierTag.getCompound(namespace)
            val map = mutableMapOf<String, Skill.Rarity>()
            for (path in mapTag.keys) {
                val rarityOrdinal = mapTag.getInt(path)
                val rarity = Skill.Rarity.entries.getOrNull(rarityOrdinal)
                if (rarity != null) {
                    map[path] = rarity
                }
            }
            rarityModifier[namespace] = map
        }

        val skillBlackListTag = tag.getCompound("skillBlackList")
        for (namespace in skillBlackListTag.keys) {
            val listTag = skillBlackListTag.getList(namespace, NbtElement.STRING_TYPE.toInt())
            val list = mutableListOf<String>()
            for (i in 0 until listTag.size) {
                list.add(listTag.getString(i))
            }
            skillBlackList[namespace] = list
        }
    }

    fun toTag(tag: NbtCompound): NbtCompound {
        tag.put("cooldownModifier", NbtCompound().apply {
            for ((namespace, map) in cooldownModifier) {
                put(namespace, NbtCompound().apply {
                    for ((path, cooldown) in map) {
                        putInt(path, cooldown)
                    }
                })
            }
        })
        tag.put("rarityModifier", NbtCompound().apply {
            for ((namespace, map) in rarityModifier) {
                put(namespace, NbtCompound().apply {
                    for ((path, rarity) in map) {
                        putInt(path, rarity.ordinal)
                    }
                })
            }
        })
        tag.put("skillBlackList", NbtCompound().apply {
            for ((namespace, list) in skillBlackList) {
                put(namespace, NbtList().apply {
                    addAll(list.map { NbtString.of(it) })
                })
            }
        })
        return tag
    }

    companion object {
        private val GSON = GsonBuilder().setPrettyPrinting().create()
        private var file: File? = null
            get() {
                if (field == null) {
                    field =
                        FabricLoader.getInstance().configDir.resolve("$MOD_ID.json").toFile()
                }
                return field
            }
        var instance = Config()

        fun load() {
            println("Loading $MOD_ID configuration file")
            try {
                val file = file!!
                if (!file.exists()) {
                    save()
                }
                if (file.exists()) {
                    val br = BufferedReader(FileReader(file))
                    val jsonElement = JsonParser.parseReader(br)
                    val config = fromJson(jsonElement.toString())
                    setInstance(config, true)
                }
            } catch (e: Exception) {
                save()
            }
        }

        fun save() {
            val json: String = try {
                instance.toJson()
            } catch (e: Exception) {
                e.localizedMessage
            }
            val file = file!!
            try {
                FileWriter(file).use { it.write(json) }
            } catch (e: Exception) {
                println("Couldn't save $MOD_ID configuration file")
                e.printStackTrace()
            }
        }

        fun setInstance(config: Config?, saveIfFailed: Boolean) {
            if (config != null) {
                instance = config
            } else if (saveIfFailed) {
                println(
                    "Read $MOD_ID configuration failed. Try to save the current configuration"
                )
                save()
            }
        }

        fun fromJson(json: String?): Config? = GSON.fromJson(json, Config::class.java)
    }
}
