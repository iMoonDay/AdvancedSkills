package com.imoonday.trigger

import com.imoonday.util.SkillSlot
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.server.network.ServerPlayerEntity

interface AttributeTrigger : UnequipTrigger {

    val attribute: Map<EntityAttribute, EntityAttributeModifier>

    fun addAttributes(player: ServerPlayerEntity) {
        attribute.forEach {
            player.attributes.getCustomInstance(it.key)?.run {
                if (hasModifier(it.value)) {
                    removeModifier(it.value)
                }
                addPersistentModifier(it.value)
            }
        }
    }

    fun removeAttributes(player: ServerPlayerEntity) {
        attribute.forEach {
            player.attributes.getCustomInstance(it.key)?.run {
                if (hasModifier(it.value)) {
                    removeModifier(it.value)
                }
            }
        }
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        removeAttributes(player)
    }
}