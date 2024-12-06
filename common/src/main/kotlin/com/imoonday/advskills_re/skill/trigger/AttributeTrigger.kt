package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.component.*
import net.minecraft.entity.attribute.*
import net.minecraft.server.network.*

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