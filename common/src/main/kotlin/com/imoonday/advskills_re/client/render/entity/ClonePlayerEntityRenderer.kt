package com.imoonday.advskills_re.client.render.entity

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.entity.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.*
import net.minecraft.client.util.math.*
import net.minecraft.util.*
import net.minecraft.util.math.*

class ClonePlayerEntityRenderer(
    ctx: EntityRendererFactory.Context,
) : BipedEntityRenderer<ClonePlayerEntity, PlayerEntityModel<ClonePlayerEntity>>(
    ctx,
    PlayerEntityModel(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f
) {

    private val defaultModel = PlayerEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER), false)
    private val slimModel = PlayerEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER_SLIM), false)
    private val defaultInnerArmor =
        ArmorEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR))
    private val slimInnerArmor =
        ArmorEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER_SLIM_INNER_ARMOR))
    private val defaultOuterArmor =
        ArmorEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR))
    private val slimOuterArmor =
        ArmorEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR))
    private val armorFeatureRenderer = ArmorFeatureRenderer(
        this,
        defaultInnerArmor,
        defaultOuterArmor,
        ctx.modelManager
    )

    init {
        this.addFeature(armorFeatureRenderer)
        this.addFeature(StuckArrowsFeatureRenderer(ctx, this))
        this.addFeature(TridentRiptideFeatureRenderer(this, ctx.modelLoader))
        this.addFeature(StuckStingersFeatureRenderer(this))
    }

    override fun getTexture(entity: ClonePlayerEntity): Identifier =
        client?.networkHandler?.getPlayerListEntry(entity.playerUUID)?.run {
            val slim = this.model == "slim"
            super.model = if (slim) slimModel else defaultModel
            armorFeatureRenderer.innerModel = if (slim) slimInnerArmor else defaultInnerArmor
            armorFeatureRenderer.outerModel = if (slim) slimOuterArmor else defaultOuterArmor
            skinTexture
        } ?: run {
            val model = DefaultSkinHelper.getModel(entity.playerUUID)
            val slim = model == "slim"
            super.model = if (slim) slimModel else defaultModel
            armorFeatureRenderer.innerModel = if (slim) slimInnerArmor else defaultInnerArmor
            armorFeatureRenderer.outerModel = if (slim) slimOuterArmor else defaultOuterArmor
            DefaultSkinHelper.getTexture(entity.playerUUID)
        }

    override fun getPositionOffset(entity: ClonePlayerEntity, f: Float): Vec3d =
        if (entity.isInSneakingPose) Vec3d(0.0, -0.125, 0.0) else super.getPositionOffset(entity, f)

    override fun scale(entity: ClonePlayerEntity, matrices: MatrixStack, amount: Float) {
        val g = 0.9375f
        matrices.scale(g, g, g)
    }

    override fun getHandSwingProgress(entity: ClonePlayerEntity, tickDelta: Float): Float {
        return super.getHandSwingProgress(entity, tickDelta)
    }
}