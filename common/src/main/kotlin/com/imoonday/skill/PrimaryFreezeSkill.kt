package com.imoonday.skill

import com.imoonday.entity.*
import com.imoonday.init.*
import com.imoonday.trigger.*
import com.imoonday.util.*
import com.mojang.blaze3d.systems.*
import net.minecraft.client.gui.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.*

class PrimaryFreezeSkill : Skill(
    id = "primary_freeze",
    types = listOf(SkillType.CONTROL),
    cooldown = 8,
    rarity = Rarity.SUPERB,
    sound = ModSounds.FIRE
), SpecialStateRenderTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val rotation = rotationVector.normalize().multiply(1.5)
            world.spawnEntity(
                FreezeEnergyBallEntity(
                    this,
                    rotationVector.x,
                    rotationVector.y,
                    rotationVector.z,
                    world
                ).apply {
                    setPosition(x + rotation.x, eyeY, z + rotation.z)
                }
            )
        }
        return UseResult.success()
    }

    override fun isInSpecialState(player: PlayerEntity): Boolean = player.isForceFrozen

    override fun renderSpecialState(context: DrawContext) {
        super.renderSpecialState(context)
        this.renderOverlay(context)
    }

    private fun renderOverlay(context: DrawContext) {
        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)
        context.drawTexture(
            FROZEN_OVERLAY,
            0,
            0,
            -90,
            0.0f,
            0.0f,
            context.scaledWindowWidth,
            context.scaledWindowHeight,
            context.scaledWindowWidth,
            context.scaledWindowHeight
        )
        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
    }

    companion object {

        private val FROZEN_OVERLAY: Identifier = Identifier("textures/misc/powder_snow_outline.png")
    }
}