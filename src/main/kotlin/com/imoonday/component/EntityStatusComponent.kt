package com.imoonday.component

import com.imoonday.init.ModComponents
import com.imoonday.init.ModEffects
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
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
    ModEffects.SYNC_CLIENT_EFFECTS
        .forEach {
            status.putBoolean(it.syncId, hasStatusEffect(it as StatusEffect))
        }
    ModComponents.STATUS.sync(this)
}

val LivingEntity.status: NbtCompound
    get() = getComponent(ModComponents.STATUS).status