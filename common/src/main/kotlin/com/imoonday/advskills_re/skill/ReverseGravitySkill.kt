package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class ReverseGravitySkill : Skill(
    Settings(
        id = "reverse_gravity",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 30,
        rarity = SkillRarity.EPIC
    )
), AutoStopTrigger,
    InvertMouseTrigger,
    FlipUpsideDownTrigger,
    EyeHeightTrigger,
    StopTrigger,
    ClientUseTrigger,
    InvertInputTrigger,
    CameraUpdateMovementTrigger,
    SaveMovingTrigger {

    init {
        settings.addParameter(
            name = PARAM_REVERSE_DURATION,
            baseValue = DEFAULT_REVERSE_DURATION,
            enhancementId = ENHANCEMENT_DURATION,
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT,
            genericText = true
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this)

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_REVERSE_DURATION, player, DEFAULT_REVERSE_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super<AutoStopTrigger>.onStop(player)
        player.startCooling()
        player.pitch = -player.pitch
    }

    override fun postStop(player: PlayerEntity) {
        super.postStop(player)
        player.calculateDimensions()
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        player.run {
            if (isUsing()) {
                val data = getActiveData()
                if (!data.getBoolean(NBT_FIRST_TICK)) {
                    velocity = velocity.withAxis(Direction.Axis.Y, 0.0)
                    pitch = -pitch
                    data.putBoolean(NBT_FIRST_TICK, true)
                }
                if (!abilities.flying) {
                    addVelocity(0.0, 0.15, 0.0)
                    velocityDirty = true
                }
                fallDistance = 0f
                if (isInSneakingPose && !isSneaking) {
                    pose = EntityPose.STANDING
                    refreshPositionAfterTeleport(pos.subtract(0.0, 0.3, 0.0))
                }
                calculateDimensions()
                if (verticalCollision) setOnGround(true)
            }
        }
        super.tick(player, usedTime)
    }

    override fun shouldInvertMouse(player: PlayerEntity): Boolean = player.isUsing()
    override fun shouldInvertInput(player: PlayerEntity): Boolean = player.isUsing()
    override fun shouldFlipUpsideDown(player: PlayerEntity): Boolean = player.isUsing()
    override fun getEyeHeight(
        player: PlayerEntity,
        original: Float,
        pose: EntityPose,
        dimensions: EntityDimensions,
    ): Float = if (player.isUsing()) dimensions.height - original else original

    override fun onStop(clientPlayer: PlayerEntity) {
        super<ClientUseTrigger>.onStop(clientPlayer)
        clientPlayer.run {
            calculateDimensions()
            pitch = -pitch
        }
    }

    override fun getDelta(original: Float, clientPlayer: PlayerEntity): Float = 1f

    companion object {

        // NBT Keys
        private const val NBT_FIRST_TICK = "First"  // 首次触发标记

        // Default Values
        private const val DEFAULT_REVERSE_DURATION = 15 * 20

        // Parameter Names
        private const val PARAM_REVERSE_DURATION = "reverse_duration"  // 反重力持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
    }
}