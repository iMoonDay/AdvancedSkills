package com.imoonday.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.imoonday.MOD_ID
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class Config {
    var offsetX: Int = 0
        set(value) {
            field = value
            save()
        }
    var offsetY: Int = 0
        set(value) {
            field = value
            save()
        }

    var lastGamma: Double = MinecraftClient.getInstance()?.options?.gamma?.value ?: 1.0
        set(value) {
            field = value
            save()
        }

    fun toJson(): String = GSON.toJson(this)

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
