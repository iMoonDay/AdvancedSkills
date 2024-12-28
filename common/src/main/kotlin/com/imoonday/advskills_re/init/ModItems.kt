package com.imoonday.advskills_re.init

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.item.*
import dev.architectury.registry.registries.*
import net.minecraft.item.*
import net.minecraft.registry.*
import java.util.function.*

object ModItems {

    @JvmField
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM)

    @JvmField
    val FRUITS: MutableList<Supplier<SkillFruitItem>> = mutableListOf()

    @JvmField
    val COMMON_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        registerFruit("common_skill_fruit") { SkillFruitItem(SkillRarity.COMMON) }

    @JvmField
    val UNCOMMON_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        registerFruit("uncommon_skill_fruit") { SkillFruitItem(SkillRarity.UNCOMMON) }

    @JvmField
    val RARE_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        registerFruit("rare_skill_fruit") { SkillFruitItem(SkillRarity.RARE) }

    @JvmField
    val SUPERB_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        registerFruit("superb_skill_fruit") { SkillFruitItem(SkillRarity.SUPERB) }

    @JvmField
    val EPIC_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        registerFruit("epic_skill_fruit") { SkillFruitItem(SkillRarity.EPIC) }

    @JvmField
    val LEGENDARY_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        registerFruit("legendary_skill_fruit") { SkillFruitItem(SkillRarity.LEGENDARY) }

    @JvmField
    val MYTHIC_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        registerFruit("mythic_skill_fruit") { SkillFruitItem(SkillRarity.MYTHIC) }

    @JvmField
    val UNIQUE_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        registerFruit("unique_skill_fruit") { SkillFruitItem(SkillRarity.UNIQUE) }

    fun init() {
        ITEMS.register()
    }

    private fun registerFruit(id: String, supplier: Supplier<SkillFruitItem>): RegistrySupplier<SkillFruitItem> =
        ITEMS.register(id, supplier).also { FRUITS.add(it) }
}