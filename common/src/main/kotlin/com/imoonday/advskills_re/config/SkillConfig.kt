package com.imoonday.advskills_re.config

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import com.mojang.logging.*
import dev.architectury.platform.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.minecraft.nbt.*
import net.minecraft.server.*
import net.minecraft.util.*
import org.slf4j.*
import java.io.*
import java.nio.file.*
import kotlin.concurrent.*
import kotlin.io.path.*

@Serializable
class SkillConfig {

    var skillCooldownMultiplier: Double = 1.0
        get() {
            if (field < 0.0) {
                field = 0.0
            }
            return field
        }
        set(value) {
            field = value.coerceAtLeast(0.0)
            save()
        }
    var skillModifier: MutableMap<String, MutableMap<String, SkillModifier>> = mutableMapOf(
        MOD_ID to mutableMapOf(
            "example" to SkillModifier(20, Skill.Rarity.COMMON, 20)
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

    fun getModifier(id: Identifier): SkillModifier? = skillModifier[id.namespace]?.get(id.path)

    fun getOrCreateModifier(id: Identifier): SkillModifier =
        skillModifier.getOrPut(id.namespace) { mutableMapOf() }.getOrPut(id.path) { SkillModifier.EMPTY }

    fun removeModifier(id: Identifier): Boolean = skillModifier[id.namespace]?.remove(id.path) != null

    fun isInBlackList(id: Identifier): Boolean = skillBlackList[id.namespace]?.contains(id.path) ?: false

    fun toJson(): String = JSON.encodeToString(serializer(), this)

    fun fromTag(tag: NbtCompound) {
        skillModifier.clear()
        skillBlackList.clear()
        defaultSkillSlots.clear()
        val skillModifierTag = tag.getCompound("skillModifier")
        for (namespace in skillModifierTag.keys) {
            val mapTag = skillModifierTag.getCompound(namespace)
            val map = mutableMapOf<String, SkillModifier>()
            for (path in mapTag.keys) {
                val modifierTag = mapTag.getCompound(path)
                map[path] = SkillModifier.fromNbt(modifierTag)
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
                        for ((path, modifier) in map) {
                            put(path, modifier.toNbt())
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

        private val LOGGER: Logger = LogUtils.getLogger()
        private val JSON = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        private var file: File = Platform.getConfigFolder().resolve("$MOD_ID.json").toFile()
        var instance = SkillConfig()
        private var loading = false
        private var saving = false

        fun load() {
            if (loading) return
            loading = true
            LOGGER.info("Loading $MOD_ID configuration file")
            try {
                val file = file
                if (!file.exists()) {
                    save()
                } else {
                    var text = file.readText(Charsets.UTF_8)
                    var times = 0
                    while (text.isEmpty() && times++ < 10) {
                        Thread.sleep(100)
                        text = file.readText(Charsets.UTF_8)
                    }
                    instance = fromJson(text)
                }
            } catch (e: Exception) {
                LOGGER.error(
                    "Read $MOD_ID configuration failed. Try to save the current configuration", e
                )
                save()
            } finally {
                loading = false
            }
        }

        fun save() {
            if (saving) return
            saving = true
            try {
                file.writeText(instance.toJson(), Charsets.UTF_8)
            } catch (e: Exception) {
                LOGGER.error("Couldn't save $MOD_ID configuration file", e)
            } finally {
                saving = false
            }
        }

        fun fromJson(json: String): SkillConfig = JSON.decodeFromString(serializer(), json)

        fun initWatchService(server: MinecraftServer) {
            val service = FileSystems.getDefault().newWatchService()
            file.parentFile.toPath().register(service, StandardWatchEventKinds.ENTRY_MODIFY)
            val fileName = file.name
            var lastEventTime = System.currentTimeMillis()

            thread(start = true, name = "Skill Config Watch Service") {
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
                        Channels.SYNC_CONFIG_S2C.sendToPlayers(
                            server.playerManager.playerList,
                            SyncConfigS2CPacket(tag)
                        )
                        lastEventTime = System.currentTimeMillis()
                    }
                    if (!key.reset()) break
                }
            }
        }
    }
}
