package com.imoonday.advskills_re.config

import com.imoonday.advskills_re.*
import com.mojang.logging.*
import dev.architectury.platform.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.fabricmc.api.*
import org.slf4j.*
import java.io.*
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*
import kotlin.concurrent.*
import kotlin.io.path.*

@Serializable
class ClientConfig {

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
    var layout: Array<IntArray> = defaultLayout
        get() {
            if (!isValidLayout(field)) field = defaultLayout
            return field
        }
        set(value) {
            if (isValidLayout(value)) field = value else return
            save()
        }
    var quickCastWheelHoldTime: Int = 250
        set(value) {
            field = value
            save()
        }

    fun toJson(): String = JSON.encodeToString(serializer(), this)

    companion object {

        private val LOGGER: Logger = LogUtils.getLogger()
        private val JSON = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        private var file: File = Platform.getConfigFolder().resolve("$MOD_ID-client.json").toFile()
        var instance = ClientConfig()
        private var loading = false
        private var saving = false

        fun load() {
            if (loading) return
            loading = true
            LOGGER.info("Loading $MOD_ID-client configuration file")
            var text: String
            try {
                val file = file
                if (!file.exists()) {
                    save()
                } else {
                    text = file.readText(Charsets.UTF_8)
                    var times = 0
                    while (text.isEmpty() && times++ < 10) {
                        Thread.sleep(100)
                        text = file.readText(Charsets.UTF_8)
                    }
                    instance = fromJson(text)
                }
            } catch (e: Exception) {
                LOGGER.error(
                    "Read $MOD_ID-client configuration failed. Try to save the current configuration", e
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
                LOGGER.error("Couldn't save $MOD_ID-client configuration file", e)
            } finally {
                saving = false
            }
        }

        fun fromJson(json: String): ClientConfig = JSON.decodeFromString(serializer(), json)

        fun initWatchService() {
            if (Platform.getEnv() != EnvType.CLIENT) return
            val service = FileSystems.getDefault().newWatchService()
            file.parentFile.toPath().register(service, ENTRY_MODIFY)
            val fileName = file.name
            var lastEventTime = System.currentTimeMillis()

            thread(start = true, name = "Client Config Watch Service") {
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

        fun isValidLayout(layout: Array<IntArray>?): Boolean {
            if (layout == null) return false
            val numbers = mutableSetOf<Int>()
            for (row in layout) {
                for (num in row) {
                    if (num < 0 || num > 10) {
                        return false
                    }
                    if (num != 0) {
                        numbers.add(num)
                    }
                }
            }
            return numbers.size == 10
        }

        val defaultLayout = arrayOf(
            intArrayOf(1, 2),
            intArrayOf(3, 4),
            intArrayOf(5, 6),
            intArrayOf(7, 8),
            intArrayOf(9, 10),
        )
    }
}
