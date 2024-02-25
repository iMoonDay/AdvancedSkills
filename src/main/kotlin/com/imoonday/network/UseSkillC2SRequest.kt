package com.imoonday.network

import com.imoonday.AdvancedSkills
import com.imoonday.components.*
import com.imoonday.init.isSilenced
import com.imoonday.trigger.LongPressTrigger
import com.imoonday.trigger.SynchronousCoolingTrigger
import com.imoonday.utils.SkillSlot
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

class UseSkillC2SRequest(
    val slot: SkillSlot,
    val keyState: KeyState,
) : FabricPacket {
    companion object {
        val id = AdvancedSkills.id("use_skill_c2s")
        val pType = PacketType.create(id) {
            UseSkillC2SRequest(SkillSlot.fromIndex(it.readInt()), it.readEnumConstant(KeyState::class.java))
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, _ ->
                val slot = packet.slot
                val keyState = packet.keyState
                if (slot.valid && !player.isSpectator) {
                    player.getSkill(slot).run {
                        if ((this !is LongPressTrigger || !player.isUsingSkill(this)) && keyState == KeyState.RELEASE) return@registerGlobalReceiver
                        if (player.isSilenced()) {
                            player.sendMessage(
                                Text.translatable(
                                    "advancedSkills.useSkill.silenced"
                                ), true
                            )
                            return@registerGlobalReceiver
                        }
                        if (player.isCooling(this)) {
                            player.sendMessage(
                                Text.translatable(
                                    "advancedSkills.useSkill.cooling",
                                    name.string,
                                    "${(player.getCooldown(this) / 20.0)}s"
                                ), true
                            )
                        } else {
                            val result = (this as? LongPressTrigger)?.let {
                                if (keyState == KeyState.PRESS) it.onPress(player) else it.onRelease(
                                    player,
                                    player.getSkillUsedTime(this)
                                )
                            } ?: use(player)
                            if (result.success) {
                                playSound(player)
                                if (result.cooling) player.startCooling(this)
                                (this as? SynchronousCoolingTrigger)?.otherSkills?.forEach { player.startCooling(it) }
                                player.sendMessage(result.message ?: name, true)
                            } else {
                                val message =
                                    result.message ?: Text.translatable("advancedSkills.useSkill.failed", name.string)
                                if (message != Text.empty())
                                    player.sendMessage(message, true)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(slot.ordinal)
        buf.writeEnumConstant(keyState)
    }

    override fun getType(): PacketType<*> = pType

    enum class KeyState {
        RELEASE,
        PRESS
    }
}