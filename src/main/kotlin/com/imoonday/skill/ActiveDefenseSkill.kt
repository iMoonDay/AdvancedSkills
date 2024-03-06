package com.imoonday.skill

import com.imoonday.component.isUsingSkill
import com.imoonday.component.modifyCooldown
import com.imoonday.component.startCooling
import com.imoonday.component.stopUsingSkill
import com.imoonday.init.ModSkills
import com.imoonday.trigger.AttributeTrigger
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
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class ActiveDefenseSkill : LongPressSkill(
    id = "active_defense",
    types = arrayOf(SkillType.DEFENSE),
    cooldown = 10,
    rarity = Rarity.SUPERB,
), DamageTrigger, AttributeTrigger, FeatureRendererTrigger {
    override val maxPressTime: Int = 10 * 10
    override val attribute: Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            MOVEMENT_SPEED_UUID,
            "Active Defense",
            -0.5,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun onPress(player: ServerPlayerEntity): UseResult {
        addAttributes(player)
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsingSkill(this)
        player.startCooling(this)
        if (pressedTime.toFloat() / maxPressTime < 0.5f) {
            player.modifyCooldown(skill) { it / 2 }
        }
        removeAttributes(player)
        return UseResult.consume()
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsingSkill(this)) return amount
        return amount * 0.8f
    }

    companion object {
        val MOVEMENT_SPEED_UUID: UUID = UUID.fromString("A0F548ED-6E81-45D3-83CD-4C2A2FE0A1DC")
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

    override fun shouldRender(player: PlayerEntity): Boolean =
        player.isUsingSkill(this) && !player.isUsingSkill(ModSkills.ABSOLUTE_DEFENSE)
}