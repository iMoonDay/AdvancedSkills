package com.imoonday.util

import com.imoonday.skill.Skill
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text

@Serializable
class UseResult(
    val success: Boolean,
    val cooling: Boolean,
    val message: Text?,
) {

    fun withSuccess(success: Boolean) = UseResult(success = success, cooling = cooling, message = message)

    fun withCooling(cooling: Boolean) = UseResult(success = success, cooling = cooling, message = message)

    fun withMessage(message: Text?) = UseResult(success = success, cooling = cooling, message = message)

    companion object {

        fun success(message: Text? = null) = UseResult(success = true, cooling = true, message = message)
        fun consume(message: Text? = null) = UseResult(success = true, cooling = false, message = message)
        fun fail(message: Text? = null) = UseResult(success = false, cooling = false, message = message)
        fun pass(message: Text? = null) = UseResult(success = false, cooling = true, message = message)
        fun of(success: Boolean, message: Text? = null) = if (success) success(message) else fail(message)
        fun of(success: Boolean, successMessage: Text? = null, failMessage: Text? = null) =
            if (success) success(successMessage) else fail(failMessage)

        fun passive(name: String) = fail(translate("useSkill", "passive", name))
        fun startUsing(user: PlayerEntity, skill: Skill, data: NbtCompound? = null, failedMessage: Text? = null) = of(
            user.startUsing(skill, data), null,
            failedMessage ?: translateActive(true, skill.name.string)
        )

        fun toggleUsing(
            user: PlayerEntity,
            skill: Skill,
            data: NbtCompound? = null,
        ): UseResult {
            val active = user.toggleUsing(skill, data)
            if (!active) user.startCooling(skill)
            return consume(translateActive(active, skill.name.string))
        }
    }
}