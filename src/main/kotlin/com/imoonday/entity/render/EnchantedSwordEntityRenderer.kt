package com.imoonday.entity.render

import com.imoonday.entity.EnchantedSwordEntity
import com.imoonday.util.client
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis

class EnchantedSwordEntityRenderer(context: EntityRendererFactory.Context) :
    EntityRenderer<EnchantedSwordEntity>(context) {

    override fun render(
        entity: EnchantedSwordEntity,
        yaw: Float,
        tickDelta: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
    ) {
        matrixStack.push()
        matrixStack.translate(0.0, 0.2, 0.0)
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - entity.getYaw(0.5f)))
        matrixStack.multiply(
            RotationAxis.NEGATIVE_X.rotationDegrees((entity.getPitch(0.5f)) + 90f)
        )
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45f))
        client!!.itemRenderer.renderItem(
            sword,
            ModelTransformationMode.GROUND,
            getLight(entity, tickDelta),
            OverlayTexture.DEFAULT_UV,
            matrixStack,
            vertexConsumerProvider,
            entity.world,
            0
        )
        matrixStack.pop()
        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, i)
    }

    override fun getTexture(entity: EnchantedSwordEntity): Identifier = Identifier("textures/item/diamond_sword.png")

    companion object {

        val sword: ItemStack = Items.DIAMOND_SWORD.defaultStack.apply {
            addEnchantment(Enchantments.LOYALTY, 1)
        }
    }
}