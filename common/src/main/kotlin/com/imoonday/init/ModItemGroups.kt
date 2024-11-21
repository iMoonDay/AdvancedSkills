package com.imoonday.init

import com.imoonday.*
import com.imoonday.util.*
import dev.architectury.registry.*
import dev.architectury.registry.registries.*
import net.minecraft.item.*
import net.minecraft.registry.*

object ModItemGroups {

    @JvmField
    val ITEM_GROUPS: DeferredRegister<ItemGroup> = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM_GROUP)
    val GROUP: RegistrySupplier<ItemGroup> = register("advanced_skills_re") {
        ModItems.UNIQUE_SKILL_FRUIT.get()?.defaultStack ?: Items.BARRIER.defaultStack
    }

    fun init() {
        ITEM_GROUPS.register()
        ModItems.ITEMS.forEach { CreativeTabRegistry.append(GROUP, it) }
//        CreativeTabRegistry.append(GROUP, *ModItems.FRUITS.toTypedArray())
//        CreativeTabRegistry.append(GROUP, *Skill.getValidSkills().mapNotNull { it.item }.toTypedArray())
    }

    private fun register(name: String, icon: () -> ItemStack): RegistrySupplier<ItemGroup> =
        ITEM_GROUPS.register(name) { CreativeTabRegistry.create(translate("itemGroup"), icon) }
}