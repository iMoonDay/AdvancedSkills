package com.imoonday.config

import com.imoonday.MOD_ID
import com.imoonday.network.SyncConfigS2CPacket
import com.imoonday.skill.Skill
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.MinecraftServer
import java.io.File
import java.io.FileWriter
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import kotlin.concurrent.thread
import kotlin.io.path.name

@Serializable
class Config {

    var skillModifier: MutableMap<String, MutableMap<String, MutableMap<String, String>>> = mutableMapOf(
        MOD_ID to mutableMapOf(
            "example" to mutableMapOf(
                "cooldown" to "20",
                "rarity" to Skill.Rarity.COMMON.id,
            )
        )
    )
    var skillBlackList: MutableMap<String, MutableList<String>> = mutableMapOf(
        MOD_ID to mutableListOf(
            "example"
        )
    )
    var defaultSkillSlots: MutableMap<String, Int> = mutableMapOf(
        "active" to 3,
        "generic" to 1,
        "passive" to 2,
    )

    fun toJson(): String = JSON.encodeToString(serializer(), this)

    fun fromTag(tag: NbtCompound) {
        skillModifier.clear()
        skillBlackList.clear()
        defaultSkillSlots.clear()
        val skillModifierTag = tag.getCompound("skillModifier")
        for (namespace in skillModifierTag.keys) {
            val mapTag = skillModifierTag.getCompound(namespace)
            val map = mutableMapOf<String, MutableMap<String, String>>()
            for (path in mapTag.keys) {
                val attributesTag = mapTag.getCompound(path)
                val attributes = mutableMapOf<String, String>()
                for (attribute in attributesTag.keys) {
                    attributes[attribute] = attributesTag.getString(attribute)
                }
                map[path] = attributes
            }
            skillModifier[namespace] = map
        }
        val skillBlackListTag = tag.getCompound("skillBlackList")
        for (namespace in skillBlackListTag.keys) {
            val listTag = skillBlackListTag.getList(namespace, NbtElement.STRING_TYPE.toInt())
            val list = mutableListOf<String>()
            repeat(listTag.size) {
                list.add(listTag.getString(it))
            }
            skillBlackList[namespace] = list
        }
        val defaultSkillSlotsTag = tag.getCompound("defaultSkillSlots")
        for (namespace in defaultSkillSlotsTag.keys) {
            defaultSkillSlots[namespace] = defaultSkillSlotsTag.getInt(namespace)
        }
    }

    fun toTag(tag: NbtCompound): NbtCompound {
        return tag.apply {
            put("skillModifier", NbtCompound().apply {
                for ((namespace, map) in skillModifier) {
                    put(namespace, NbtCompound().apply {
                        for ((path, attributes) in map) {
                            put(path, NbtCompound().apply {
                                for ((key, value) in attributes) {
                                    putString(key, value)
                                }
                            })
                        }
                    })
                }
            })
            put("skillBlackList", NbtCompound().apply {
                for ((namespace, list) in skillBlackList) {
                    put(namespace, NbtList().apply {
                        addAll(list.map { NbtString.of(it) })
                    })
                }
            })
            put("defaultSkillSlots", NbtCompound().apply {
                for ((slot, count) in defaultSkillSlots) {
                    putInt(slot, count)
                }
            })
        }
    }

    companion object {

        private val JSON = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        private var file: File = FabricLoader.getInstance().configDir.resolve("$MOD_ID.json").toFile()
        var instance = Config()
        private var loading = false
        private var saving = false

        fun load() {
            if (loading) return
            loading = true
            println("Loading $MOD_ID configuration file")
            try {
                val file = file
                if (!file.exists()) {
                    save()
                }
                if (file.exists()) {
                    var text = file.readText(Charsets.UTF_8)
                    var times = 0
                    while (text.isEmpty() && times++ < 10) {
                        Thread.sleep(100)
                        text = file.readText(Charsets.UTF_8)
                    }
                    instance = fromJson(text)
                }
            } catch (e: Exception) {
                println(
                    "Read $MOD_ID configuration failed. Try to save the current configuration"
                )
                e.printStackTrace()
                save()
            }
            loading = false
        }

        fun save() {
            if (saving) return
            saving = true
            val json: String = try {
                instance.toJson()
            } catch (e: Exception) {
                e.localizedMessage
            }
            val file = file
            try {
                FileWriter(file).use { it.write(json) }
            } catch (e: Exception) {
                println("Couldn't save $MOD_ID configuration file")
                e.printStackTrace()
            }
            saving = false
        }

        fun fromJson(json: String): Config = JSON.decodeFromString(serializer(), json)

        fun initWatchService(server: MinecraftServer) {
            val service = FileSystems.getDefault().newWatchService()
            file.parentFile.toPath().register(service, StandardWatchEventKinds.ENTRY_MODIFY)
            val fileName = file.name
            var lastEventTime = System.currentTimeMillis()

            thread(start = true, name = "Config Watch Service") {
                while (true) {
                    val key = service.take()
                    if (key.pollEvents().any {
                            (it.context() as Path).fileName.name == fileName && it.kind() == StandardWatchEventKinds.ENTRY_MODIFY
                        }
                        && System.currentTimeMillis() - lastEventTime > 1000
                        && !saving && !loading
                    ) {
                        load()
                        val tag = instance.toTag(NbtCompound())
                        PlayerLookup.all(server).forEach {
                            ServerPlayNetworking.send(it, SyncConfigS2CPacket(tag))
                        }
                        lastEventTime = System.currentTimeMillis()
                    }
                    if (!key.reset()) break
                }
            }
        }
    }
}
