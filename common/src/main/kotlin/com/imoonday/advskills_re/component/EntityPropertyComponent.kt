package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.effect.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.network.s2c.*
import net.minecraft.entity.*
import net.minecraft.nbt.*
import net.minecraft.server.world.*

class EntityPropertyComponent(override val entity: Entity) : Component<Entity> {

    var dirty = false
    override var synced = false
    var properties: NbtCompound = NbtCompound()

    override fun readFromNbt(tag: NbtCompound) {
        if (tag.contains("properties")) {
            properties = tag.getCompound("properties")
        }
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.put("properties", properties)
    }

    override fun serverTick() {
        if (dirty) {
            sync()
            dirty = false
        }
    }

    fun onEffectsChanged() {
        if (entity is LivingEntity) {
            properties.remove("syncEffects")
            val effects = entity.statusEffects
                .map { it.effectType }
                .filterIsInstance<SyncClientEffect>()
            if (effects.isNotEmpty()) {
                properties.put("syncEffects", NbtCompound().apply {
                    effects.forEach { put(it.syncId, it.writeData(entity)) }
                })
            }
            dirty = true
        }
    }

    override fun requestSync() {
        Channels.REQUEST_SYNC_COMPONENT_C2S.sendToServer(
            RequestSyncComponentC2SRequest(
                entity.id,
                RequestSyncComponentC2SRequest.ComponentType.ENTITY_PROPERTIES,
                RequestSyncComponentC2SRequest.Receiver.SENDER
            )
        )
    }

    override fun sync() {
        (entity.world as? ServerWorld)?.let {
            RequestSyncComponentC2SRequest.Receiver.NEARBY_PLAYERS.send(
                Channels.SYNC_PROPERTIES_S2C,
                SyncPropertiesS2CPacket(entity.id, toNbt()),
                null,
                entity
            )
        }
    }
}

val Entity.propertyComponent: EntityPropertyComponent
    get() = (this as Propertied).propertyComponent
var Entity.properties: NbtCompound
    get() = propertyComponent.properties
    set(value) {
        propertyComponent.properties = value
        syncProperties()
    }

fun Entity.syncProperties() {
    if (!world.isClient) propertyComponent.dirty = true
}