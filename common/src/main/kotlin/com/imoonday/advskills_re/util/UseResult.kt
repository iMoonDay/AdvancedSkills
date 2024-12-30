package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.skill.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.sound.*
import net.minecraft.text.*

/**
 * Result of a skill use
 * @param success play sound and send message if true, otherwise send message only
 * @param cooling start cooling if true
 * @param message send message if not null, otherwise send the default failure message. If the content is empty, do not send any message
 * @param sound play sound if not null, otherwise do not play any sound
 */
class UseResult(
    val success: Boolean,
    val cooling: Boolean,
    val message: Text?,
    val sound: SoundEvent? = null
) {

    fun withSuccess(success: Boolean) =
        UseResult(success = success, cooling = cooling, message = message, sound = sound)

    fun withCooling(cooling: Boolean) =
        UseResult(success = success, cooling = cooling, message = message, sound = sound)

    fun withMessage(message: Text?) = UseResult(success = success, cooling = cooling, message = message, sound = sound)

    fun withSound(sound: SoundEvent?) =
        UseResult(success = success, cooling = cooling, message = message, sound = sound)

    companion object {

        /**
         * @param message optional message to display to the user
         * @return UseResult with success and start cooling
         */
        @JvmOverloads
        @JvmStatic
        fun success(message: Text? = null, sound: SoundEvent? = null) =
            UseResult(success = true, cooling = true, message = message, sound = sound)

        /**
         * @param message optional message to display to the user
         * @return UseResult with success and no cooling
         */
        @JvmOverloads
        @JvmStatic
        fun consume(message: Text? = null, sound: SoundEvent? = null) =
            UseResult(success = true, cooling = false, message = message, sound = sound)

        /**
         * @param message optional message to display to the user
         * @return UseResult with failure and no cooling
         */
        @JvmOverloads
        @JvmStatic
        fun fail(message: Text? = null, sound: SoundEvent? = null) =
            UseResult(success = false, cooling = false, message = message, sound = sound)

        /**
         * @param message optional message to display to the user
         * @return UseResult with failure and start cooling
         */
        @JvmOverloads
        @JvmStatic
        fun pass(message: Text? = null, sound: SoundEvent? = null) =
            UseResult(success = false, cooling = true, message = message, sound = sound)

        /**
         * @param success true for success, false for failure
         * @param message optional message to display to the user
         */
        @JvmStatic
        fun of(success: Boolean, message: Text? = null) = of(success, message, message)

        /**
         * @param success true for success, false for failure
         * @param successMessage optional message to display to the user if success
         * @param failMessage optional message to display to the user if failure
         */
        @JvmStatic
        fun of(success: Boolean, successMessage: Text? = null, failMessage: Text? = null) =
            if (success) success(successMessage) else fail(failMessage)

        /**
         * @param name name of the skill
         * @return UseResult with failure and no cooling and message "$name is passive skill"
         */
        @JvmStatic
        fun passive(name: Text) = fail(translate("useSkill.passive", name))

        /**
         * @param user player who is using the skill
         * @param skill skill being used
         * @param data optional data for the skill
         * @param failedMessage optional message to display to the user if failure
         * @return UseResult with result of whether the skill was successfully started or not
         */
        @JvmOverloads
        @JvmStatic
        fun startUsing(
            user: PlayerEntity,
            skill: Skill,
            data: NbtCompound? = null,
            failedMessage: Text? = null,
            onStart: (() -> Unit)? = null,
        ): UseResult = if (user.startUsing(skill, data)) {
            onStart?.invoke()
            consume(null)
        } else fail(failedMessage ?: translateActive(skill, true))

        /**
         * @param user player who is using the skill
         * @param skill skill being used
         * @param data optional data for the skill
         * @param onStart optional callback to be executed when the skill is successfully started
         * @return UseResult with result of whether the skill was successfully toggled or not
         */
        @JvmOverloads
        @JvmStatic
        fun toggleUsing(
            user: PlayerEntity,
            skill: Skill,
            data: NbtCompound? = null,
            startCoolingAfterStop: Boolean = true,
            onStart: (() -> Unit)? = null,
        ): UseResult {
            val active = user.toggleUsing(skill, data)
            if (active) onStart?.invoke() else if (startCoolingAfterStop) user.startCooling(skill)
            return consume(translateActive(skill, active))
        }
    }
}