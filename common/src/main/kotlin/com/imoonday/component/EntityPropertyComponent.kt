package com.imoonday.component

import com.imoonday.advanced_skills_re.api.*
import com.imoonday.effect.*
import com.imoonday.network.*
import com.imoonday.network.s2c.*
import net.minecraft.entity.*
import net.minecraft.nbt.*
import net.minecraft.server.world.*

interface PropertyComponent : Component {

    var properties: NbtCompound
}

class EntityPropertyComponent(private val entity: Entity) : PropertyComponent {

    var dirty = false
    override var properties: NbtCompound = NbtCompound()
        set(value) {
            field = value
            entity.propertyComponent.sync()
        }

    override fun readFromNbt(tag: NbtCompound) {
        properties = tag.getCompound("properties")
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.put("properties", properties)
    }

    override fun serverTick() {
        if (entity is LivingEntity) {
            properties.put("syncEffects", NbtList().apply {
                addAll(
                    entity.statusEffects.map { it.effectType }
                        .filterIsInstance<SyncClientEffect>()
                        .map { NbtString.of(it.syncId) })
            })
        }
        if (dirty) {
            sync()
            dirty = false
        }
    }

    override fun sync() {
        (entity.world as? ServerWorld)?.let {
            Channels.SYNC_PROPERTIES_S2C.sendToPlayers(
                it.players,
                SyncPropertiesS2CPacket(entity.id, toNbt())
            )
        }
    }
}

val Entity.propertyComponent: EntityPropertyComponent
    get() = (this as Propertied).propertyComponent
var Entity.properties: NbtCompound
    get() = propertyComponent.run {
        dirty = true
        properties
    }
    set(value) {
        this.propertyComponent.properties = value
    }