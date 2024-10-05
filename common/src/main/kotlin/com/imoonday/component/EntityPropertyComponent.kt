package com.imoonday.component

import com.imoonday.effect.SyncClientEffect
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString

interface PropertyComponent : Component {

    var properties: NbtCompound
}

class EntityPropertyComponent(private val entity: Entity) :
    PropertyComponent,
    AutoSyncedComponent,
    ServerTickingComponent {

    override var properties: NbtCompound = NbtCompound()
        set(value) {
            field = value
            Components.PROPERTY.sync(entity)
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
        Components.PROPERTY.sync(entity)
    }
}

var Entity.properties: NbtCompound
    get() = getComponent(Components.PROPERTY).properties
    set(value) {
        getComponent(Components.PROPERTY).properties = value
    }