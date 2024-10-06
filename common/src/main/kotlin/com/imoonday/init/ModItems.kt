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
    val COMMON_SKILL_FRUIT = SkillFruitItem(Skill.Rarity.COMMON).register("common_skill_fruit")

    @JvmField
    val UNCOMMON_SKILL_FRUIT = SkillFruitItem(Skill.Rarity.UNCOMMON).register("uncommon_skill_fruit")

    @JvmField
    val RARE_SKILL_FRUIT = SkillFruitItem(Skill.Rarity.RARE).register("rare_skill_fruit")

    @JvmField
    val SUPERB_SKILL_FRUIT = SkillFruitItem(Skill.Rarity.SUPERB).register("superb_skill_fruit")

    @JvmField
    val EPIC_SKILL_FRUIT = SkillFruitItem(Skill.Rarity.EPIC).register("epic_skill_fruit")

    @JvmField
    val LEGENDARY_SKILL_FRUIT = SkillFruitItem(Skill.Rarity.LEGENDARY).register("legendary_skill_fruit")

    @JvmField
    val MYTHIC_SKILL_FRUIT = SkillFruitItem(Skill.Rarity.MYTHIC).register("mythic_skill_fruit")

    @JvmField
    val UNIQUE_SKILL_FRUIT = SkillFruitItem(Skill.Rarity.UNIQUE).register("unique_skill_fruit")

    fun <T : Item> T.register(id: String): RegistrySupplier<T> {
        if (this is SkillFruitItem) FRUITS.add(this)
        return ITEMS.register(id) { this }
    }

    fun init() = Unit
}