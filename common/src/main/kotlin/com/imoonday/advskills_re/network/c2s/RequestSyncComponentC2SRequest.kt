package com.imoonday.advskills_re.network.c2s

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.mixin.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.util.*
import dev.architectury.networking.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.network.*
import net.minecraft.server.network.*
import net.minecraft.server.world.*

class RequestSyncComponentC2SRequest(
    val entityId: Int,
    val type: ComponentType,
    val receiver: Receiver
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(
        buf.readInt(),
        buf.readEnumConstant(ComponentType::class.java),
        buf.readEnumConstant(Receiver::class.java)
    )

    override fun encode(buf: PacketByteBuf) {
        buf.writeInt(entityId)
        buf.writeEnumConstant(type)
        buf.writeEnumConstant(receiver)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        (context.player as? ServerPlayerEntity)?.let { player ->
            player.serverWorld.getEntityById(entityId)?.let { target ->
                type.createPacket(target)?.let { packet ->
                    receiver.send(type.channel, packet, player, target)
                }
            }
        }
    }

    enum class ComponentType(val channel: NetworkChannel) {
        ENTITY_PROPERTIES(Channels.SYNC_PROPERTIES_S2C) {

            override fun createPacket(entity: Entity): NetworkPacket =
                SyncPropertiesS2CPacket(entity.id, entity.propertyComponent.toNbt())
        },
        PLAYER_DATA(Channels.SYNC_PLAYER_DATA_S2C) {

            override fun createPacket(entity: Entity): NetworkPacket? =
                (entity as? PlayerEntity)?.run { SyncPlayerDataS2CPacket(id, data.toNbt()) }
        };

        abstract fun createPacket(entity: Entity): NetworkPacket?
    }

    enum class Receiver {
        SENDER {

            override fun send(
                channel: NetworkChannel,
                packet: NetworkPacket,
                sender: ServerPlayerEntity?,
                target: Entity
            ) {
                sender?.run { channel.sendToPlayer(this, packet) }
            }
        },
        ALL_PLAYERS {

            override fun send(
                channel: NetworkChannel,
                packet: NetworkPacket,
                sender: ServerPlayerEntity?,
                target: Entity
            ) {
                (sender ?: target).server?.run { channel.sendToPlayers(playerManager.playerList, packet) }
            }
        },
        NEARBY_PLAYERS {

            override fun send(
                channel: NetworkChannel,
                packet: NetworkPacket,
                sender: ServerPlayerEntity?,
                target: Entity
            ) = channel.sendToPlayers(tracking(target), packet)

            private fun tracking(entity: Entity): Collection<ServerPlayerEntity> {
                val manager = entity.world.chunkManager

                if (manager is ServerChunkManager) {
                    val storage = manager.threadedAnvilChunkStorage
                    val tracker = (storage as ThreadedAnvilChunkStorageAccessor).entityTrackers.get(entity.id)

                    if (tracker != null) {
                        val players: MutableList<ServerPlayerEntity> = mutableListOf()
                        if (entity is ServerPlayerEntity) {
                            players += entity
                        }
                        return players + tracker.playersTracking.map { it.player }
                    }
                }

                return emptySet()
            }
        },
        WORLD_PLAYERS {

            override fun send(
                channel: NetworkChannel,
                packet: NetworkPacket,
                sender: ServerPlayerEntity?,
                target: Entity
            ) {
                (target.world as? ServerWorld)?.run { channel.sendToPlayers(players, packet) }
            }
        };

        abstract fun send(
            channel: NetworkChannel,
            packet: NetworkPacket,
            sender: ServerPlayerEntity?,
            target: Entity
        )
    }
}