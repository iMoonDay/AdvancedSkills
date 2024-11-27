package com.imoonday.advskills_re.entity.render

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.model.json.*
import net.minecraft.client.util.math.*
import net.minecraft.enchantment.*
import net.minecraft.item.*
import net.minecraft.util.*
import net.minecraft.util.math.*

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