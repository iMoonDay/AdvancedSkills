package com.imoonday.components

import com.imoonday.init.ModComponents
import com.imoonday.init.isForceFrozen
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound

interface StatusComponent : Component {
    var status: NbtCompound
}

class EntityStatusComponent(private val entity: LivingEntity) : StatusComponent, AutoSyncedComponent {
    override var status: NbtCompound = NbtCompound()
        set(value) {
            field = value
            ModComponents.STATUS.sync(entity)
        }

    override fun readFromNbt(tag: NbtCompound) {
        status = tag.getCompound("status")
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.put("status", status)
    }
}

fun LivingEntity.syncStatus() {
    if (world.isClient) return
    status.putBoolean("frozen", isForceFrozen)
    ModComponents.STATUS.sync(this)
}

val LivingEntity.status: NbtCompound
    get() = getComponent(ModComponents.STATUS).status