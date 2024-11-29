package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class ReverseGravitySkill : Skill(
    id = "reverse_gravity",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 30,
    rarity = Rarity.EPIC,
), AutoStopTrigger,
    InvertMouseTrigger,
    FlipUpsideDownTrigger,
    EyeHeightTrigger,
    StopTrigger,
    ClientUseTrigger,
    InvertInputTrigger,
    CameraUpdateMovementTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this)

    override val persistTime: Int = 20 * 15

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling()
        player.pitch = -player.pitch
        super<AutoStopTrigger>.onStop(player)
    }

    override fun postStop(player: PlayerEntity) {
        super.postStop(player)
        player.calculateDimensions()
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        player.run {
            if (isUsing()) {
                val usingData = getUsingData()
                if (usingData?.getBoolean("first") != true) {
                    velocity = velocity.withAxis(Direction.Axis.Y, 0.0)
                    pitch = -pitch
                    usingData?.putBoolean("first", true)
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

    override fun onStop(player: PlayerEntity) {
        super<ClientUseTrigger>.onStop(player)
        player.run {
            calculateDimensions()
            pitch = -pitch
        }
    }

    override fun getDelta(original: Float, player: PlayerEntity): Float = 1f
}