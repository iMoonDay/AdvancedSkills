package com.imoonday.trigger

import com.imoonday.util.SkillSlot
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.server.network.ServerPlayerEntity

interface AttributeTrigger : UnequipTrigger {

    fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = emptyMap()

    fun ServerPlayerEntity.addAttributes() {
        this@AttributeTrigger.getAttributes().forEach {
            attributes.getCustomInstance(it.key)?.run {
                if (hasModifier(it.value)) {
                    removeModifier(it.value)
                }
                addPersistentModifier(it.value)
            }
        }
    }

    fun ServerPlayerEntity.removeAttributes() {
        this@AttributeTrigger.getAttributes().forEach {
            attributes.getCustomInstance(it.key)?.run {
                if (hasModifier(it.value)) {
                    removeModifier(it.value)
                }
            }
        }
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        player.removeAttributes()
    }
}