package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.clientPlayer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Direction

class ReverseGravitySkill : Skill(
    id = "reverse_gravity",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 30,
    rarity = Rarity.EPIC,
), AutoStopTrigger,
    WorldRenderTrigger,
    InvertMouseTrigger,
    FlipUpsideDownTrigger,
    EyeHeightTrigger,
    StopTrigger,
    ClientUseTrigger,
    InvertInputTrigger,
    CameraUpdateMovementTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this)

    override fun getPersistTime(): Int = 20 * 10

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
                if (getUsingData()?.getBoolean("first") != true) {
                    velocity = velocity.withAxis(Direction.Axis.Y, 0.0)
                    pitch = -pitch
                    getUsingData()?.putBoolean("first", true)
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

    override fun apply(matrixStack: MatrixStack, tickDelta: Float, client: MinecraftClient) {
        super.apply(matrixStack, tickDelta, client)
        if (client.player?.isUsing() == true) matrixStack.scale(-1f, -1f, 1f)
    }

    override fun shouldInvertMouse(): Boolean = clientPlayer?.isUsing() == true

    override fun shouldInvertInput(): Boolean = clientPlayer?.isUsing() == true

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

    override fun getDelta(original: Float): Float = 1f
}