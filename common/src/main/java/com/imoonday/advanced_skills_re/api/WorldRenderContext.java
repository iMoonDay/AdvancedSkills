package com.imoonday.advanced_skills_re.api;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * From Fabric API
 */
public interface WorldRenderContext {

    WorldRenderer worldRenderer();

    MatrixStack matrixStack();

    float tickDelta();

    Camera camera();

    GameRenderer gameRenderer();

    LightmapTextureManager lightmapTextureManager();

    Matrix4f projectionMatrix();

    ClientWorld world();

    Profiler profiler();

    boolean advancedTranslucency();

    @Nullable
    VertexConsumerProvider consumers();

    @Nullable
    Frustum frustum();
}
