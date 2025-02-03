package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.client.ClientConfig;
import com.imoonday.advskills_re.client.render.entity.feature.StatusEffectRenderer;
import com.imoonday.advskills_re.client.render.skill.SkillRendererHandler;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Shadow
    public Camera camera;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    public <E extends Entity> void render(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        SkillRendererHandler.renderEntity(this.camera, entity, yaw, tickDelta, matrices, vertexConsumers, light);
        if (!ClientConfig.get().getDisableStatusEffectRenderers() && entity instanceof LivingEntity livingEntity) {
            matrices.push();
            matrices.scale(-1.0F, -1.0F, 1.0F);
            matrices.translate(0.0F, -1.0F, 0.0F);
            StatusEffectRenderer.render(livingEntity, yaw, tickDelta, matrices, vertexConsumers, light);
            matrices.pop();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", shift = At.Shift.AFTER))
    public <E extends Entity> void renderPostEntity(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity livingEntity)) return;

        matrices.push();
        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.translate(0.0F, -1.0F, 0.0F);
        SkillRendererHandler.renderPostLivingEntity(livingEntity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.pop();
    }
}
