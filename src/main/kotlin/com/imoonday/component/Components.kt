package com.imoonday.component

import com.imoonday.util.id
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import net.minecraft.entity.Entity

object Components : EntityComponentInitializer {

    @JvmField
    val DATA: ComponentKey<DataComponent> =
        ComponentRegistry.getOrCreate(id("data"), DataComponent::class.java)

    @JvmField
    val PROPERTY: ComponentKey<PropertyComponent> =
        ComponentRegistry.getOrCreate(id("property"), PropertyComponent::class.java)

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(DATA, ::PlayerDataComponent, RespawnCopyStrategy.CHARACTER)
        registry.registerFor(Entity::class.java, PROPERTY, ::EntityPropertyComponent)
    }
}