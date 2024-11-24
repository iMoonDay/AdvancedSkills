package com.imoonday.advanced_skills_re.mixin;

import com.imoonday.client.ClientTriggerHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Shadow
    public Camera camera;
    @Unique
    private MinecraftClient advanced_skills_re$client;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftClient client, TextureManager textureManager, ItemRenderer itemRenderer, BlockRenderManager blockRenderManager, TextRenderer textRenderer, GameOptions gameOptions, EntityModelLoader modelLoader, CallbackInfo ci) {
        this.advanced_skills_re$client = client;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    public <E extends Entity> void render(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ClientTriggerHandler.INSTANCE.renderAfterEntity(advanced_skills_re$client, this.camera, entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
