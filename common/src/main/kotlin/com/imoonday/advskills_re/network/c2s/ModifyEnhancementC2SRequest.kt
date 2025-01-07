package com.imoonday.advskills_re.network.c2s

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import com.mojang.logging.*
import dev.architectury.networking.*
import net.minecraft.network.*
import net.minecraft.server.network.*

data class ModifyEnhancementC2SRequest(
    val skill: Skill,
    val enhancement: String,
    val operation: Operation,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(
        Skills.fromId(buf.readIdentifier()),
        buf.readString(),
        buf.readEnumConstant(Operation::class.java)
    )

    override fun encode(buf: PacketByteBuf) {
        buf.writeIdentifier(skill.id)
        buf.writeString(enhancement)
        buf.writeEnumConstant(operation)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        val player = context.player as? ServerPlayerEntity ?: return
        if (skill.disabled || !player.hasLearned(skill)) return

        val data = player.getEnhancement(skill, enhancement)?.second
        if (data != null) {
            operation.operate(data)
            player.syncData()
        } else {
            LOGGER.warn(
                "Received nonexistent enhancement of skill ${skill.name} with id $enhancement from player ${player.displayName}"
            )
        }
    }

    enum class Operation {
        LEVEL_UP {

            override fun operate(data: EnhancementData) = data.levelUp()
        },
        LEVEL_DOWN {

            override fun operate(data: EnhancementData) = data.levelDown()
        },
        ACTIVATE {

            override fun operate(data: EnhancementData) = data.activate()
        },
        DEACTIVATE {

            override fun operate(data: EnhancementData) = data.deactivate()
        };

        abstract fun operate(data: EnhancementData)
    }

    companion object {

        private val LOGGER = LogUtils.getLogger()
    }
}