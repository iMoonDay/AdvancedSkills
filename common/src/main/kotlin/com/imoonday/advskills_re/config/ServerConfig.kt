package com.imoonday.advskills_re.config

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.mixin.*
import com.mojang.logging.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.minecraft.server.*
import net.minecraft.util.*
import org.slf4j.*
import java.io.*
import java.nio.file.*

@Serializable
data class ServerConfig(
    var disableSkillFruitGeneration: Boolean = false,
    var oakLeavesDropChance: Float = 0.005f,
    var darkOakLeavesDropChance: Float = 0.005f,
    var ancientCityChestGenerationChance: Float = 0.25f,
    var buriedTreasureChestGenerationChance: Float = 0.25f,
    var endCityTreasureChestGenerationChance: Float = 0.25f,
) {

    fun toJson(): String = JSON.encodeToString(serializer(), this)

    companion object {

        private val LOGGER: Logger = LogUtils.getLogger()
        private val JSON = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        private val SERVER_CONFIG: WorldSavePath = WorldSavePath("serverconfig")
        private lateinit var file: File
        private val DEFAULT_INSTANCE = ServerConfig()

        private var instance = ServerConfig()

        fun get(): ServerConfig = instance

        fun init(server: MinecraftServer) {
            file = File(getServerConfigPath(server).toFile(), "$MOD_ID-server.json")
            load()
            if (instance != DEFAULT_INSTANCE) {
                tryReloadDataPacks(server)
            }
        }

        fun load() {
            LOGGER.info("Loading $MOD_ID-server configuration file")
            try {
                if (!file.exists()) {
                    save()
                } else {
                    instance = fromJson(file.readText(Charsets.UTF_8))
                }
            } catch (e: Exception) {
                LOGGER.error(
                    "Read $MOD_ID-server configuration failed. Try to save the current configuration", e
                )
                save()
            }
        }

        fun save() {
            try {
                file.writeText(instance.toJson(), Charsets.UTF_8)
            } catch (e: Exception) {
                LOGGER.error("Couldn't save $MOD_ID-server configuration file", e)
            }
        }

        @JvmStatic
        fun fromJson(json: String): ServerConfig = JSON.decodeFromString(serializer(), json)

        private fun getServerConfigPath(server: MinecraftServer): Path {
            val serverConfig = server.getSavePath(SERVER_CONFIG)
            if (!Files.isDirectory(serverConfig)) {
                try {
                    Files.createDirectories(serverConfig)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            return serverConfig
        }

        private fun tryReloadDataPacks(server: MinecraftServer) {
            val resourcePackManager = server.dataPackManager
            val saveProperties = server.saveProperties
            val enabledNames = resourcePackManager.enabledNames
            val dataPacks =
                ReloadCommandAccessor.callFindNewDataPacks(resourcePackManager, saveProperties, enabledNames)
            server.reloadResources(dataPacks)
        }
    }
}