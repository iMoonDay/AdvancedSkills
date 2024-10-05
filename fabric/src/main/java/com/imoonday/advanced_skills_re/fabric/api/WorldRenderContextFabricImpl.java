package com.imoonday.advanced_skills_re.fabric.api;

import com.imoonday.advanced_skills_re.api.WorldRenderContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class WorldRenderContextFabricImpl implements WorldRenderContext {

    private final net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context;

    private WorldRenderContextFabricImpl(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        this.context = context;
    }

    public static WorldRenderContextFabricImpl of(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        return new WorldRenderContextFabricImpl(context);
    }

    @Override
    public WorldRenderer worldRenderer() {
        return this.context.worldRenderer();
    }

    @Override
    public MatrixStack matrixStack() {
        return this.context.matrixStack();
    }

    @Override
    public float tickDelta() {
        return this.context.tickDelta();
    }

    @Override
    public Camera camera() {
        return this.context.camera();
    }

    @Override
    public GameRenderer gameRenderer() {
        return this.context.gameRenderer();
    }

    @Override
    public LightmapTextureManager lightmapTextureManager() {
        return this.context.lightmapTextureManager();
    }

    @Override
    public Matrix4f projectionMatrix() {
        return this.context.projectionMatrix();
    }

    @Override
    public ClientWorld world() {
        return this.context.world();
    }

    @Override
    public Profiler profiler() {
        return this.context.profiler();
    }

    @Override
    public boolean advancedTranslucency() {
        return this.context.advancedTranslucency();
    }

    @Override
    public @Nullable VertexConsumerProvider consumers() {
        return this.context.consumers();
    }

    @Override
    public @Nullable Frustum frustum() {
        return this.context.frustum();
    }
}
