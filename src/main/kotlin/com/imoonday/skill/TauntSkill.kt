package com.imoonday.skill

import com.imoonday.component.isUsingSkill
import com.imoonday.entity.Servant
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
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.RotationAxis

class TauntSkill : Skill(
    id = "taunt",
    types = arrayOf(SkillType.ENHANCEMENT),
    cooldown = 30,
    rarity = Rarity.SUPERB,
), DamageTrigger, AutoStopTrigger, FeatureRendererTrigger {

    override val persistTime: Int = 20 * 15
    override val skill: Skill
        get() = this

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)
    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsingSkill(this) || attacker !is Servant) amount else amount * 0.75f

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
        matrices.push()
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
        matrices.translate(-0.5, 0.65, -0.5)
        val model = context.modelManager.getModel(modelIdentifier)
        context.itemRenderer.renderBakedItemQuads(
            matrices,
            provider.getBuffer(TexturedRenderLayers.getEntityTranslucentCull()),
            model.getQuads(null, null, player.random),
            ItemStack.EMPTY,
            0xF000F0,
            OverlayTexture.DEFAULT_UV
        )
        matrices.pop()
    }

    override fun shouldRender(player: PlayerEntity): Boolean = player.isUsingSkill(this)
}