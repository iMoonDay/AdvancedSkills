package com.imoonday.advskills_re.network

import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.util.*
import dev.architectury.networking.*
import net.minecraft.network.*

object Channels {

    @JvmStatic
    val USE_SKILL_C2S: NetworkChannel = registerChannel("use_skill_c2s")

    @JvmStatic
    val EQUIP_SKILL_C2S: NetworkChannel = registerChannel("equip_skill_c2s")

    @JvmStatic
    val SEND_PLAYER_DATA_C2S: NetworkChannel = registerChannel("send_player_data_c2s")

    @JvmStatic
    val CHOOSE_SKILL_C2S: NetworkChannel = registerChannel("choose_skill_c2s")

    @JvmStatic
    val REFRESH_CHOICE_C2S: NetworkChannel = registerChannel("refresh_choice_c2s")

    @JvmStatic
    val SYNC_CONFIG_S2C: NetworkChannel = registerChannel("sync_config_s2c")

    @JvmStatic
    val LEARN_SKILL_S2C: NetworkChannel = registerChannel("learn_skill_s2c")

    @JvmStatic
    val SYNC_PROPERTIES_S2C: NetworkChannel = registerChannel("sync_properties_s2c")

    @JvmStatic
    val SYNC_PLAYER_DATA_S2C: NetworkChannel = registerChannel("sync_player_data_s2c")

    @JvmStatic
    val REQUEST_SYNC_COMPONENT_C2S: NetworkChannel = registerChannel("request_sync_component_c2s")

    @JvmStatic
    val UPDATE_ORE_CACHE_S2C: NetworkChannel = registerChannel("update_ore_cache_s2c")

    @JvmStatic
    val ENHANCE_SKILL_S2C: NetworkChannel = registerChannel("enhance_skill_s2c")

    @JvmStatic
    val UPDATE_JUMPING_C2S: NetworkChannel = registerChannel("update_jumping_c2s")

    @JvmStatic
    val SYNC_RARITIES_S2C: NetworkChannel = registerChannel("sync_rarities_s2c")

    @JvmStatic
    val SYNC_SETTINGS_S2C: NetworkChannel = registerChannel("sync_settings_s2c")

    @JvmStatic
    val MODIFY_ENHANCEMENT_C2S: NetworkChannel = registerChannel("modify_enhancement_c2s")

    @JvmStatic
    val KEY_PRESSED_C2S: NetworkChannel = registerChannel("key_pressed_c2s")

    fun register() {
        USE_SKILL_C2S.register(::UseSkillC2SRequest)
        EQUIP_SKILL_C2S.register(::EquipSkillC2SRequest)
        SEND_PLAYER_DATA_C2S.register(::SendPlayerDataC2SPacket)
        CHOOSE_SKILL_C2S.register(::ChooseSkillC2SRequest)
        REFRESH_CHOICE_C2S.register { RefreshChoiceC2SRequest }
        SYNC_CONFIG_S2C.register(::SyncConfigS2CPacket)
        LEARN_SKILL_S2C.register(::LearnSkillS2CPacket)
        SYNC_PROPERTIES_S2C.register(::SyncPropertiesS2CPacket)
        SYNC_PLAYER_DATA_S2C.register(::SyncPlayerDataS2CPacket)
        REQUEST_SYNC_COMPONENT_C2S.register(::RequestSyncComponentC2SRequest)
        UPDATE_ORE_CACHE_S2C.register(::UpdateOreCacheS2CPacket)
        ENHANCE_SKILL_S2C.register(::EnhanceSkillS2CPacket)
        UPDATE_JUMPING_C2S.register(::UpdateJumpingC2SPacket)
        SYNC_RARITIES_S2C.register(::SyncRaritiesS2CPacket)
        SYNC_SETTINGS_S2C.register(::SyncSettingsS2CPacket)
        MODIFY_ENHANCEMENT_C2S.register(::ModifyEnhancementC2SRequest)
    }

    private fun registerChannel(name: String) = NetworkChannel.create(id(name))

    private inline fun <reified T : NetworkPacket> NetworkChannel.register(
        noinline decoder: (PacketByteBuf) -> T,
    ) = register(
        T::class.java,
        { packet, buf -> packet.encode(buf) },
        decoder
    ) { packet, ctx -> ctx.get().run { queue { packet.apply(this) } } }
}