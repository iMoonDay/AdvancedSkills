package com.imoonday.init

import com.imoonday.*
import com.imoonday.item.*
import com.imoonday.skill.*
import dev.architectury.registry.registries.*
import net.minecraft.item.*
import net.minecraft.registry.*

object ModItems {

    @JvmField
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM)

    @JvmField
    val FRUITS: MutableList<SkillFruitItem> = mutableListOf()

    @JvmField
    val COMMON_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        ITEMS.register("common_skill_fruit") { SkillFruitItem(Skill.Rarity.COMMON) }

    @JvmField
    val UNCOMMON_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        ITEMS.register("uncommon_skill_fruit") { SkillFruitItem(Skill.Rarity.UNCOMMON) }

    @JvmField
    val RARE_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        ITEMS.register("rare_skill_fruit") { SkillFruitItem(Skill.Rarity.RARE) }

    @JvmField
    val SUPERB_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        ITEMS.register("superb_skill_fruit") { SkillFruitItem(Skill.Rarity.SUPERB) }

    @JvmField
    val EPIC_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        ITEMS.register("epic_skill_fruit") { SkillFruitItem(Skill.Rarity.EPIC) }

    @JvmField
    val LEGENDARY_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        ITEMS.register("legendary_skill_fruit") { SkillFruitItem(Skill.Rarity.LEGENDARY) }

    @JvmField
    val MYTHIC_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        ITEMS.register("mythic_skill_fruit") { SkillFruitItem(Skill.Rarity.MYTHIC) }

    @JvmField
    val UNIQUE_SKILL_FRUIT: RegistrySupplier<SkillFruitItem> =
        ITEMS.register("unique_skill_fruit") { SkillFruitItem(Skill.Rarity.UNIQUE) }

    fun init() {
        ITEMS.register()
    }
}