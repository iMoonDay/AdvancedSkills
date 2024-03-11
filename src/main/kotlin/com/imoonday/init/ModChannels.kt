package com.imoonday.init

import com.imoonday.network.*

object ModChannels {

    fun registerServer() {
        UseSkillC2SRequest.register()
        EquipSkillC2SRequest.register()
        SyncConfigS2CPacket.register()
        SendPlayerDataC2SPacket.register()
    }

    fun registerClient() {
        LearnSkillS2CPacket.register()
    }
}