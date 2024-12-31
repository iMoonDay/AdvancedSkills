package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class ReverseGravitySkill : Skill(
    id = "reverse_gravity",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 30,
    rarity = SkillRarity.EPIC,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME)
), AutoStopTrigger,
    InvertMouseTrigger,
    FlipUpsideDownTrigger,
    EyeHeightTrigger,
    StopTrigger,
    ClientUseTrigger,
    InvertInputTrigger,
    CameraUpdateMovementTrigger {

    init {
        addParameter(
            name = timeParamName,
            baseValue = 15 * 20,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this)

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
                if (!data.getBoolean("first")) {
                    velocity = velocity.withAxis(Direction.Axis.Y, 0.0)
                    pitch = -pitch
                    data.putBoolean("first", true)
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
}