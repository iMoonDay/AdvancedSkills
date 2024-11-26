package com.imoonday.advskills_re.forge.mixin;

import com.imoonday.advskills_re.api.WorldRenderEvents;
import com.imoonday.advskills_re.forge.api.WorldRenderContextForgeImpl;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Shadow
    private Frustum frustum;

    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Shadow
    private @Nullable ClientWorld world;

    @Shadow
    private @Nullable PostEffectProcessor transparencyPostProcessor;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderChunkDebugInfo(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/Camera;)V"))
    private void onChunkDebugRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        WorldRenderEvents.LAST.invoker().last(new WorldRenderContextForgeImpl((WorldRenderer) (Object) this, matrices, tickDelta, camera, frustum, gameRenderer, lightmapTextureManager, projectionMatrix, bufferBuilders.getEntityVertexConsumers(), world.getProfiler(), transparencyPostProcessor != null, world));
    }
}
