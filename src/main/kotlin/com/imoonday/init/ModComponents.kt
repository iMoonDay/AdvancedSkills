package com.imoonday.init

import com.imoonday.component.DataComponent
import com.imoonday.component.EntityPropertyComponent
import com.imoonday.component.PlayerDataComponent
import com.imoonday.component.PropertyComponent
import com.imoonday.util.id
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import net.minecraft.entity.Entity

object ModComponents : EntityComponentInitializer {

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