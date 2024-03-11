package com.imoonday.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.imoonday.MOD_ID
import io.wispforest.owo.config.annotation.Config
import net.fabricmc.loader.api.FabricLoader
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

@Config(name = "ui-config", wrapperName = "UIConfig")
class UIConfigModel {

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
    var nameScrollRate: Double = 1.0

    fun toJson(): String = GSON.toJson(this)

    companion object {

        private val GSON = GsonBuilder().setPrettyPrinting().create()
        private var file: File? = null
            get() {
                if (field == null) {
                    field =
                        FabricLoader.getInstance().configDir.resolve("$MOD_ID-client.json").toFile()
                }
                return field
            }
        var instance = UIConfigModel()

        fun load() {
            println("Loading $MOD_ID-client configuration file")
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
                println("Couldn't save $MOD_ID-client configuration file")
                e.printStackTrace()
            }
        }

        fun setInstance(config: UIConfigModel?, saveIfFailed: Boolean) {
            if (config != null) {
                instance = config
            } else if (saveIfFailed) {
                println(
                    "Read $MOD_ID-client configuration failed. Try to save the current configuration"
                )
                save()
            }
        }

        fun fromJson(json: String?): UIConfigModel? = GSON.fromJson(json, UIConfigModel::class.java)
    }
}
