package com.imoonday.advskills_re.network

import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.util.*
import dev.architectury.networking.*
import net.minecraft.network.*

object Channels {

    val USE_SKILL_C2S: NetworkChannel = registerChannel("use_skill_c2s")
    val EQUIP_SKILL_C2S: NetworkChannel = registerChannel("equip_skill_c2s")
    val SEND_PLAYER_DATA_C2S: NetworkChannel = registerChannel("send_player_data_c2s")
    val CHOOSE_SKILL_C2S: NetworkChannel = registerChannel("choose_skill_c2s")
    val REFRESH_CHOICE_C2S: NetworkChannel = registerChannel("refresh_choice_c2s")
    val SYNC_CONFIG_S2C: NetworkChannel = registerChannel("sync_config_s2c")
    val LEARN_SKILL_S2C: NetworkChannel = registerChannel("learn_skill_s2c")
    val SYNC_PROPERTIES_S2C: NetworkChannel = registerChannel("sync_properties_s2c")
    val SYNC_PLAYER_DATA_S2C: NetworkChannel = registerChannel("sync_player_data_s2c")
    val REQUEST_SYNC_COMPONENT_C2S: NetworkChannel = registerChannel("request_sync_component_c2s")
    val SPAWN_PARTICLES_S2C: NetworkChannel = registerChannel("spawn_particles_s2c")
    val UPDATE_ORE_CACHE_S2C: NetworkChannel = registerChannel("update_ore_cache_s2c")

    fun register() {
        USE_SKILL_C2S.register(::UseSkillC2SRequest)
        EQUIP_SKILL_C2S.register(::EquipSkillC2SRequest)
        SEND_PLAYER_DATA_C2S.register(::SendPlayerDataC2SPacket)
        CHOOSE_SKILL_C2S.register(::ChooseSkillC2SRequest)
        REFRESH_CHOICE_C2S.register(::RefreshChoiceC2SRequest)
        SYNC_CONFIG_S2C.register(::SyncConfigS2CPacket)
        LEARN_SKILL_S2C.register(::LearnSkillS2CPacket)
        SYNC_PROPERTIES_S2C.register(::SyncPropertiesS2CPacket)
        SYNC_PLAYER_DATA_S2C.register(::SyncPlayerDataS2CPacket)
        REQUEST_SYNC_COMPONENT_C2S.register(::RequestSyncComponentC2SRequest)
        SPAWN_PARTICLES_S2C.register(::SpawnParticlesS2CPacket)
        UPDATE_ORE_CACHE_S2C.register(::UpdateOreCacheS2CPacket)
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