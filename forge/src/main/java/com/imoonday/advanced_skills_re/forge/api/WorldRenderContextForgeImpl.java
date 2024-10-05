package com.imoonday.advanced_skills_re.forge.api;

import com.imoonday.advanced_skills_re.api.WorldRenderContext;
import com.imoonday.advanced_skills_re.mixin.WorldRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class WorldRenderContextForgeImpl implements WorldRenderContext {

    private final WorldRenderer worldRenderer;
    private final MatrixStack matrixStack;
    private final float tickDelta;
    private final Camera camera;
    private final Frustum frustum;
    private final GameRenderer gameRenderer;
    private final LightmapTextureManager lightmapTextureManager;
    private final Matrix4f projectionMatrix;
    private final VertexConsumerProvider consumers;
    private final Profiler profiler;
    private final boolean advancedTranslucency;
    private final ClientWorld world;

    public WorldRenderContextForgeImpl(WorldRenderer worldRenderer, MatrixStack matrixStack, float tickDelta, Camera camera, Frustum frustum, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, VertexConsumerProvider consumers, Profiler profiler, boolean advancedTranslucency, ClientWorld world) {
        this.worldRenderer = worldRenderer;
        this.matrixStack = matrixStack;
        this.tickDelta = tickDelta;
        this.camera = camera;
        this.frustum = frustum;
        this.gameRenderer = gameRenderer;
        this.lightmapTextureManager = lightmapTextureManager;
        this.projectionMatrix = projectionMatrix;
        this.consumers = consumers;
        this.profiler = profiler;
        this.advancedTranslucency = advancedTranslucency;
        this.world = world;
    }

    @Override
    public WorldRenderer worldRenderer() {
        return worldRenderer;
    }

    @Override
    public MatrixStack matrixStack() {
        return matrixStack;
    }

    @Override
    public float tickDelta() {
        return tickDelta;
    }

    @Override
    public Camera camera() {
        return camera;
    }

    @Override
    public GameRenderer gameRenderer() {
        return gameRenderer;
    }

    @Override
    public LightmapTextureManager lightmapTextureManager() {
        return lightmapTextureManager;
    }

    @Override
    public Matrix4f projectionMatrix() {
        return projectionMatrix;
    }

    @Override
    public ClientWorld world() {
        return world;
    }

    @Override
    public Profiler profiler() {
        return profiler;
    }

    @Override
    public boolean advancedTranslucency() {
        return advancedTranslucency;
    }

    @Override
    public @Nullable VertexConsumerProvider consumers() {
        return consumers;
    }

    @Override
    public @Nullable Frustum frustum() {
        return frustum;
    }

    public static WorldRenderContextForgeImpl of(RenderLevelStageEvent event) {
        WorldRendererAccessor levelRenderer = (WorldRendererAccessor) event.getLevelRenderer();
        VertexConsumerProvider.Immediate consumers = levelRenderer.getBufferBuilders().getEntityVertexConsumers();
        GameRenderer gameRenderer = MinecraftClient.getInstance().gameRenderer;
        return new WorldRenderContextForgeImpl(event.getLevelRenderer(), event.getPoseStack(), event.getPartialTick(), event.getCamera(), event.getFrustum(), gameRenderer, gameRenderer.getLightmapTextureManager(), event.getProjectionMatrix(), consumers, levelRenderer.getWorld().getProfiler(), levelRenderer.getTransparencyPostProcessor() != null, levelRenderer.getWorld());
    }
}
