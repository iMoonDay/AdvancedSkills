package com.imoonday.network

object Channels {
    fun registerServer() {
        UseSkillC2SRequest.register()
        EquipSkillC2SRequest.register()
        UpdateVelocityC2SPacket.register()
    }

    fun registerClient() {
        LearnSkillS2CPacket.register()
    }
}