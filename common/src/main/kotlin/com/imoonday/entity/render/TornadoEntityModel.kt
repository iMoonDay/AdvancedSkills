package com.imoonday.entity.render

import com.imoonday.entity.TornadoEntity
import net.minecraft.client.model.*
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack

class TornadoEntityModel(root: ModelPart) : EntityModel<TornadoEntity>() {

    private val line1: ModelPart = root.getChild("line1")
    private val line2: ModelPart = root.getChild("line2")
    private val line3: ModelPart = root.getChild("line3")
    private val line4: ModelPart = root.getChild("line4")
    private val line5: ModelPart = root.getChild("line5")
    private val line6: ModelPart = root.getChild("line6")
    private val line7: ModelPart = root.getChild("line7")

    override fun setAngles(
        entity: TornadoEntity,
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
        line1.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha)
        line2.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha)
        line3.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha)
        line4.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha)
        line5.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha)
        line6.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha)
        line7.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha)
    }

    companion object {

        val texturedModelData: TexturedModelData
            get() {
                val modelData = ModelData()
                modelData.root.apply {
                    addChild(
                        "line1",
                        ModelPartBuilder.create()
                            .uv(8, 18)
                            .cuboid(-1.0f, -1.0f, -1.0f, 2.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(4, 18)
                            .cuboid(-1.0f, -1.0f, 1.0f, 2.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.pivot(0.0f, 24.0f, 0.0f)
                    ).addChild(
                        "cube_r1",
                        ModelPartBuilder.create().uv(0, 18).cuboid(-1.0f, 1.5f, 1.0f, 2.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(16, 17).cuboid(-1.0f, 1.5f, -1.0f, 2.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.of(0.0f, -2.5f, 0.0f, 0.0f, 1.5708f, 0.0f)
                    )
                    addChild(
                        "line2",
                        ModelPartBuilder.create().uv(8, 17).cuboid(-2.0f, -3.0f, 2.0f, 4.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(16, 15).cuboid(-2.0f, -3.0f, -2.0f, 4.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.pivot(0.0f, 24.0f, 0.0f)
                    ).addChild(
                        "cube_r2",
                        ModelPartBuilder.create()
                            .uv(16, 14)
                            .cuboid(-2.0f, -0.5f, 2.0f, 4.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(0, 17)
                            .cuboid(-2.0f, -0.5f, -2.0f, 4.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.of(0.0f, -2.5f, 0.0f, 0.0f, 1.5708f, 0.0f)
                    )
                    addChild(
                        "line3",
                        ModelPartBuilder.create()
                            .uv(16, 13)
                            .cuboid(-3.0f, -3.0f, 3.0f, 6.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(16, 12)
                            .cuboid(-3.0f, -3.0f, -3.0f, 6.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.pivot(0.0f, 22.0f, 0.0f)
                    ).addChild(
                        "cube_r3",
                        ModelPartBuilder.create().uv(0, 16).cuboid(-3.0f, -2.5f, 3.0f, 6.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(12, 16).cuboid(-3.0f, -2.5f, -3.0f, 6.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.of(0.0f, -0.5f, 0.0f, 0.0f, 1.5708f, 0.0f)
                    )
                    addChild(
                        "line4",
                        ModelPartBuilder.create().uv(0, 15).cuboid(-4.0f, -3.0f, 4.0f, 8.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(0, 14).cuboid(-4.0f, -3.0f, -4.0f, 8.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.pivot(0.0f, 20.0f, 0.0f)
                    ).addChild(
                        "cube_r4",
                        ModelPartBuilder.create().uv(0, 12).cuboid(-4.0f, -2.5f, 4.0f, 8.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(0, 13).cuboid(-4.0f, -2.5f, -4.0f, 8.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.of(0.0f, -0.5f, 0.0f, 0.0f, 1.5708f, 0.0f)
                    )
                    addChild(
                        "line5",
                        ModelPartBuilder.create()
                            .uv(0, 11)
                            .cuboid(-5.0f, -3.0f, 5.0f, 10.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(0, 10)
                            .cuboid(-5.0f, -3.0f, -5.0f, 10.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.pivot(0.0f, 18.0f, 0.0f)
                    ).addChild(
                        "cube_r5",
                        ModelPartBuilder.create().uv(0, 8).cuboid(-5.0f, -2.5f, 5.0f, 10.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(0, 9).cuboid(-5.0f, -2.5f, -5.0f, 10.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.of(0.0f, -0.5f, 0.0f, 0.0f, 1.5708f, 0.0f)
                    )
                    addChild(
                        "line6",
                        ModelPartBuilder.create().uv(0, 7).cuboid(-6.0f, -3.0f, 6.0f, 12.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(0, 6).cuboid(-6.0f, -3.0f, -6.0f, 12.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.pivot(0.0f, 16.0f, 0.0f)
                    ).addChild(
                        "cube_r6",
                        ModelPartBuilder.create().uv(0, 4).cuboid(-6.0f, -2.5f, 6.0f, 12.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(0, 5).cuboid(-6.0f, -2.5f, -6.0f, 12.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.of(0.0f, -0.5f, 0.0f, 0.0f, 1.5708f, 0.0f)
                    )
                    addChild(
                        "line7",
                        ModelPartBuilder.create().uv(0, 3).cuboid(-7.0f, -3.0f, 7.0f, 14.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(0, 2).cuboid(-7.0f, -3.0f, -7.0f, 14.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.pivot(0.0f, 14.0f, 0.0f)
                    ).addChild(
                        "cube_r7",
                        ModelPartBuilder.create().uv(0, 0).cuboid(-7.0f, -2.5f, 7.0f, 14.0f, 1.0f, 0.0f, Dilation(0.0f))
                            .uv(0, 1).cuboid(-7.0f, -2.5f, -7.0f, 14.0f, 1.0f, 0.0f, Dilation(0.0f)),
                        ModelTransform.of(0.0f, -0.5f, 0.0f, 0.0f, 1.5708f, 0.0f)
                    )
                }
                return TexturedModelData.of(modelData, 32, 32)
            }
    }
}