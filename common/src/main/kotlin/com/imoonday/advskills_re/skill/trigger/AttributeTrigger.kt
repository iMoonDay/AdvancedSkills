package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.component.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

interface AttributeTrigger : UnequipTrigger {

    fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = emptyMap()

    fun ServerPlayerEntity.addAttributes() {
        this@AttributeTrigger.getAttributes(this).forEach {
            attributes.getCustomInstance(it.key)?.run {
                val modifier = it.value
                if (hasModifier(modifier)) {
                    removeModifier(modifier)
                }
                addPersistentModifier(modifier)
            }
        }
    }

    fun ServerPlayerEntity.removeAttributes() {
        this@AttributeTrigger.getAttributes(this).forEach {
            attributes.getCustomInstance(it.key)?.run {
                if (hasModifier(it.value)) {
                    removeModifier(it.value)
                }
            }
        }
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) = player.removeAttributes()
}