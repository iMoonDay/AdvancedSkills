package com.imoonday.entity.render

import com.imoonday.entity.MagnetEntity
import net.minecraft.client.model.*
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack

class MagnetEntityModel(root: ModelPart) : EntityModel<MagnetEntity>() {

    private val main: ModelPart = root.getChild("main")

    override fun setAngles(
        entity: MagnetEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float,
    ) = Unit

    override fun render(
        matrices: MatrixStack,
        vertexConsumer: VertexConsumer,
        light: Int,
        overlay: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float,
    ) {
        main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha)
    }

    companion object {

        val texturedModelData: TexturedModelData
            get() {
                val modelData = ModelData()
                modelData.root.addChild(
                    "main",
                    ModelPartBuilder.create().uv(18, 18).cuboid(-3.0f, -6.0f, -3.0f, 6.0f, 6.0f, 6.0f, Dilation(0.0f)),
                    ModelTransform.pivot(0.0f, 24.0f, 0.0f)
                ).apply {
                    addChild(
                        "cube_r1",
                        ModelPartBuilder.create()
                            .uv(0, 0)
                            .cuboid(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f, Dilation(0.0f)),
                        ModelTransform.of(0.0f, -3.0f, 0.0f, -0.7854f, 0.7854f, 0.0f)
                    )

                    addChild(
                        "cube_r2",
                        ModelPartBuilder.create()
                            .uv(0, 12)
                            .cuboid(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f, Dilation(0.0f)),
                        ModelTransform.of(0.0f, -3.0f, 0.0f, -0.7854f, -0.7854f, 0.0f)
                    )

                    addChild(
                        "cube_r3",
                        ModelPartBuilder.create()
                            .uv(18, 6)
                            .cuboid(0.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f, Dilation(0.0f)),
                        ModelTransform.of(0.0f, -6.0f, 0.0f, -0.7854f, 0.0f, 1.5708f)
                    )
                }
                return TexturedModelData.of(modelData, 64, 64)
            }
    }
}