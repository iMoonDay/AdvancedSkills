package com.imoonday.skill

import com.imoonday.component.isUsingSkill
import com.imoonday.component.stopUsingSkill
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.DamageTrigger
import com.imoonday.trigger.FeatureRendererTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis
import kotlin.math.cos
import kotlin.math.sin

class AbsoluteDefenseSkill : Skill(
    id = "absolute_defense",
    types = arrayOf(SkillType.DEFENSE),
    cooldown = 30,
    rarity = Rarity.SUPERB
), DamageTrigger, AutoStopTrigger, FeatureRendererTrigger {

    override val persistTime = 20 * 30
    override val skill: Skill
        get() = this

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)
    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsingSkill(this) || amount <= 0) return amount
        player.world.playSound(null, player.blockPos, SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS)
        player.stopUsingSkill(this)
        return 0.0f
    }

    override fun <T : PlayerEntity, M : EntityModel<T>> render(
        matrices: MatrixStack,
        provider: VertexConsumerProvider,
        light: Int,
        player: T,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float,
        renderer: FeatureRendererContext<T, M>,
        context: EntityRendererFactory.Context,
    ) {
        val age: Float = player.age + tickDelta
        val rotateAngleY = age / -20.0f
        val rotateAngleX: Float = sin(age / 5.0f) / 4.0f
        val rotateAngleZ: Float = cos(age / 5.0f) / 4.0f

        for (c in 0 until 4) {
            matrices.push()

            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180 + rotateAngleZ * (180f / Math.PI.toFloat())))
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotateAngleY * (180f / Math.PI.toFloat()) + (c * (360f / 4))))
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotateAngleX * (180f / Math.PI.toFloat())))
            matrices.translate(-0.5, -0.65, -0.5)

            matrices.translate(0f, 0f, -0.75f)

            val model: BakedModel = context.modelManager.getModel(modelIdentifier)
            for (dir in Direction.entries) {
                context.itemRenderer.renderBakedItemQuads(
                    matrices,
                    provider.getBuffer(TexturedRenderLayers.getEntityTranslucentCull()),
                    model.getQuads(null, dir, player.random).ifEmpty {
                        model.getQuads(null, null, player.random)
                    },
                    ItemStack.EMPTY,
                    0xF000F0,
                    OverlayTexture.DEFAULT_UV
                )
            }
            matrices.pop()
        }
    }

    override fun shouldRender(player: PlayerEntity): Boolean = player.isUsingSkill(this)
}
