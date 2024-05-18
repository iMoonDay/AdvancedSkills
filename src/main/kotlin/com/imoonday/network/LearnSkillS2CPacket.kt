package com.imoonday.network

import com.imoonday.init.ModSounds
import com.imoonday.screen.component.SkillToast
import com.imoonday.skill.Skill
import com.imoonday.util.client
import com.imoonday.util.id
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Util

class LearnSkillS2CPacket(
    val skill: Skill,
    val toast: Boolean,
) : FabricPacket {

    companion object {

        val id = id("learn_skill_s2c")
        val pType = PacketType.create(id) {
            LearnSkillS2CPacket(Skill.fromId(it.readIdentifier()), it.readBoolean())
        }!!
        private val learningHistory = mutableListOf<Skill>()
        private var lastPlaySoundTime = 0L

        fun register() {
            ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                client?.let {
                    val skill = packet.skill
                    if (skill !in learningHistory && packet.toast) {
                        learningHistory.add(skill)
                        it.toastManager.add(SkillToast(skill))
                    }
                    if (Util.getMeasuringTimeMs() - lastPlaySoundTime > 500) {
                        it.soundManager.play(PositionedSoundInstance.master(ModSounds.NOTICE, 1.0f, 1.0f))
                        lastPlaySoundTime = Util.getMeasuringTimeMs()
                    }
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeIdentifier(skill.id)
        buf.writeBoolean(toast)
    }

    override fun getType(): PacketType<*> = pType
}