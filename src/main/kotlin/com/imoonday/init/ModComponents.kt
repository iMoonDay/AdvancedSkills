package com.imoonday.init

import com.imoonday.AdvancedSkills
import com.imoonday.components.*
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy


object ModComponents : EntityComponentInitializer {
    @JvmField
    val EXP: ComponentKey<IntComponent> =
        ComponentRegistry.getOrCreate(AdvancedSkills.id("exp"), IntComponent::class.java)

    @JvmField
    val SKILLS: ComponentKey<Skill2IntMapComponent> =
        ComponentRegistry.getOrCreate(AdvancedSkills.id("skills"), Skill2IntMapComponent::class.java)

    @JvmField
    val EQUIPPED_SKILLS: ComponentKey<SkillListComponent> =
        ComponentRegistry.getOrCreate(AdvancedSkills.id("equipped_skills"), SkillListComponent::class.java)

    @JvmField
    val USING_SKILLS: ComponentKey<Skill2NbtComponent> =
        ComponentRegistry.getOrCreate(AdvancedSkills.id("using_skills"), Skill2NbtComponent::class.java)

    @JvmField
    val DAMAGED_TIME: ComponentKey<LongComponent> =
        ComponentRegistry.getOrCreate(AdvancedSkills.id("damaged_time"), LongComponent::class.java)

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(EXP, ::SkillExpComponent, RespawnCopyStrategy.CHARACTER)
        registry.registerForPlayers(SKILLS, ::LearnedSkillComponent, RespawnCopyStrategy.CHARACTER)
        registry.registerForPlayers(EQUIPPED_SKILLS, ::EquippedSkillComponent, RespawnCopyStrategy.CHARACTER)
        registry.registerForPlayers(USING_SKILLS, ::UsingSkillComponent, RespawnCopyStrategy.NEVER_COPY)
        registry.registerForPlayers(DAMAGED_TIME, ::DamagedTimeComponent, RespawnCopyStrategy.NEVER_COPY)
    }
}