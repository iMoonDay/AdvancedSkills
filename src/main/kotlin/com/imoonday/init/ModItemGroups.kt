package com.imoonday.init

import com.imoonday.skills.Skills
import com.imoonday.utils.id
import com.imoonday.utils.translate
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.ItemGroup
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

object ModItemGroups {
    val ITEM_GROUP_KEY = register("advanced_skills")
    val GROUP: ItemGroup = FabricItemGroup.builder()
        .icon { ModItems.UNIQUE_SKILL_FRUIT.defaultStack ?: Items.BARRIER.defaultStack }
        .displayName(translate("key", "category"))
        .entries { _, entries ->
            Skills.SKILLS.filterNot { it.isEmpty }.mapNotNull { it.item }.forEach { entries.add(it.defaultStack) }
        }
        .build()!!

    fun init() {
        GROUP.register()
    }

    private fun register(id: String): RegistryKey<ItemGroup> {
        return RegistryKey.of(RegistryKeys.ITEM_GROUP, id(id))!!
    }

    private fun ItemGroup.register() = Registry.register(Registries.ITEM_GROUP, ITEM_GROUP_KEY, this)!!
}