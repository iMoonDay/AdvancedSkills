package com.imoonday.init

import com.imoonday.network.*

object ModChannels {

    fun register() {
        UseSkillC2SRequest.register()
        EquipSkillC2SRequest.register()
        SendPlayerDataC2SPacket.register()
    }

    fun registerClient() {
        SyncConfigS2CPacket.register()
        LearnSkillS2CPacket.register()
    }
}