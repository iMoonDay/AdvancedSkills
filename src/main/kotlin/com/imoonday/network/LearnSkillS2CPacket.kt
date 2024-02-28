package com.imoonday.network

import com.imoonday.utils.id
import com.imoonday.init.ModSounds
import com.imoonday.screen.components.SkillToast
import com.imoonday.skills.Skills
import com.imoonday.utils.Skill
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.network.PacketByteBuf

class LearnSkillS2CPacket(
    val skill: Skill,
) : FabricPacket {
    companion object {
        val id = id("learn_skill_s2c")
        val pType = PacketType.create(id) {
            LearnSkillS2CPacket(Skills.get(it.readIdentifier()))
        }!!

        fun register() {
            ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                MinecraftClient.getInstance()?.let {
                    it.toastManager.add(SkillToast(packet.skill))
                    it.soundManager.play(PositionedSoundInstance.master(ModSounds.NOTICE, 1.0f, 1.0f))
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeIdentifier(skill.id)
    }

    override fun getType(): PacketType<*> = pType
}