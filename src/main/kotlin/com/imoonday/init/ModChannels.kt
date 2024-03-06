package com.imoonday.init

import com.imoonday.network.*

object ModChannels {
    fun registerServer() {
        UseSkillC2SRequest.register()
        EquipSkillC2SRequest.register()
        UpdateVelocityC2SPacket.register()
        SyncConfigS2CPacket.register()
    }

    fun registerClient() {
        LearnSkillS2CPacket.register()
    }
}