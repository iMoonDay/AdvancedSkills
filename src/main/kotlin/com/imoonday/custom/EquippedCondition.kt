package com.imoonday.custom

import com.imoonday.skill.Skill
import com.imoonday.util.hasEquipped
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier

@Serializable
@SerialName("equipped")
class EquippedCondition(
    override val data: Map<String, String> = mutableMapOf(),
    override val actions: ActionGroup,
    override val failedActions: ActionGroup? = null,
) : Condition {

    override fun execute(player: PlayerEntity): Boolean = data["id"]?.let {
        Skill.fromIdNullable(Identifier.tryParse(it))
    }?.let {
        return@let player.hasEquipped(it)
    } ?: false
}