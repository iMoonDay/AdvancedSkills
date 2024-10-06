package com.imoonday.network

import com.imoonday.util.*
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

    fun register() {
        USE_SKILL_C2S.register(::UseSkillC2SRequest)
        EQUIP_SKILL_C2S.register(::EquipSkillC2SRequest)
        SEND_PLAYER_DATA_C2S.register(::SendPlayerDataC2SPacket)
        CHOOSE_SKILL_C2S.register(::ChooseSkillC2SRequest)
        REFRESH_CHOICE_C2S.register { _ -> RefreshChoiceC2SRequest() }
        SYNC_CONFIG_S2C.register(::SyncConfigS2CPacket)
        LEARN_SKILL_S2C.register(::LearnSkillS2CPacket)
    }

    private fun registerChannel(name: String) = NetworkChannel.create(id(name))

    private inline fun <reified T : NetworkPacket> NetworkChannel.register(
        noinline decoder: (PacketByteBuf) -> T,
    ) = register(
        T::class.java,
        { packet, buf -> packet.encode(buf) },
        decoder
    ) { packet, ctx -> packet.apply(ctx.get()) }
}