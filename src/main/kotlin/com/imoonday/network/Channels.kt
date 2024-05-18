package com.imoonday.network

object Channels {

    fun register() {
        UseSkillC2SRequest.register()
        EquipSkillC2SRequest.register()
        SendPlayerDataC2SPacket.register()
        ChooseSkillC2SRequest.register()
        RefreshChoiceC2SRequest.register()
    }

    fun registerClient() {
        SyncConfigS2CPacket.register()
        LearnSkillS2CPacket.register()
    }
}