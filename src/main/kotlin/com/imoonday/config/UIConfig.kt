package com.imoonday.config

import com.imoonday.MOD_ID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileWriter
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import kotlin.concurrent.thread
import kotlin.io.path.name

@Serializable
class UIConfig {

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
        set(value) {
            field = value
            save()
        }
    var simplify: Boolean = false
        set(value) {
            field = value
            save()
        }

    fun toJson(): String = JSON.encodeToString(serializer(), this)

    companion object {

        private val JSON = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        private var file: File = FabricLoader.getInstance().configDir.resolve("$MOD_ID-client.json").toFile()
        var instance = UIConfig()
        private var loading = false
        private var saving = false

        fun load() {
            if (loading) return
            loading = true
            println("Loading $MOD_ID-client configuration file")
            var text = ""
            try {
                val file = file
                if (!file.exists()) {
                    save()
                }
                if (file.exists()) {
                    text = file.readText(Charsets.UTF_8)
                    var times = 0
                    while (text.isEmpty() && times++ < 10) {
                        Thread.sleep(100)
                        text = file.readText(Charsets.UTF_8)
                    }
                    instance = fromJson(text)
                }
            } catch (e: Exception) {
                println(
                    "Read $MOD_ID-client configuration failed. Try to save the current configuration"
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
                println("Couldn't save $MOD_ID-client configuration file")
                e.printStackTrace()
            }
            saving = false
        }

        fun fromJson(json: String): UIConfig = JSON.decodeFromString(serializer(), json)

        fun initWatchService() {
            if (FabricLoader.getInstance().environmentType != EnvType.CLIENT) return
            val service = FileSystems.getDefault().newWatchService()
            file.parentFile.toPath().register(service, ENTRY_MODIFY)
            val fileName = file.name
            var lastEventTime = System.currentTimeMillis()

            thread(start = true, name = "UIConfig Watch Service") {
                while (true) {
                    val key = service.take()
                    if (key.pollEvents().any {
                            (it.context() as Path).fileName.name == fileName && it.kind() == ENTRY_MODIFY
                        }
                        && System.currentTimeMillis() - lastEventTime > 1000
                        && !saving && !loading
                    ) {
                        load()
                        lastEventTime = System.currentTimeMillis()
                    }
                    if (!key.reset()) break
                }
            }
        }
    }
}
