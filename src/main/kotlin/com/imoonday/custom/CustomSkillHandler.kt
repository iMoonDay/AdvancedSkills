package com.imoonday.custom

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path

object CustomSkillHandler {

    private val module = SerializersModule {
        polymorphic(Task::class) {
            //actions
            subclass(JumpAction::class)
            subclass(MessageAction::class)
            //conditions
            subclass(EquippedCondition::class)
            //return
            subclass(ReturnImpl::class)
            //trigger
            subclass(TriggerImpl::class)
        }
    }
    val JSON = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        serializersModule = module
    }
    private var path: Path? = null
        get() {
            if (field == null) {
                field = FabricLoader.getInstance()
                    .gameDir.resolve("custom-skills")
            }
            return field
        }

    fun load() {
        path!!.toFile().mkdirs()
        Files.walk(path).toList()
            .filter { Files.isRegularFile(it) && it.toString().endsWith(".json") }
            .mapNotNull {
                val jsonContent = Files.readString(it)
                val jsonObject = JSON.parseToJsonElement(jsonContent).jsonObject
                CustomSkill.fromJson(jsonObject)
            }
            .forEach { it.register() }
    }

    fun save(skill: CustomSkill) {
        path!!.toFile().mkdirs()
        Files.writeString(path!!.resolve(skill.id.toUnderscoreSeparatedString() + ".json"), skill.toJson().toString())
    }
}