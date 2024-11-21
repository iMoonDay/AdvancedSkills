package com.imoonday.advanced_skills_re.forge.mixin;

import com.imoonday.advanced_skills_re.api.LivingEntityFeatureRenderEvent;
import com.imoonday.advanced_skills_re.api.RegistrationHelperImpl;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityRenderers.class)
public class EntityRenderersMixin {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "reloadEntityRenderers", at = @At(value = "RETURN"))
    private static void createEntityRenderer(EntityRendererFactory.Context ctx, CallbackInfoReturnable<Map<EntityType<?>, EntityRenderer<?>>> cir) {
        for (Map.Entry<EntityType<?>, EntityRenderer<?>> entry : cir.getReturnValue().entrySet()) {
            EntityRenderer<?> entityRenderer = entry.getValue();
            if (entityRenderer instanceof LivingEntityRenderer renderer) { // Must be living for features
                LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor) entityRenderer;
                LivingEntityFeatureRenderEvent.EVENT.invoker().registerRenderers((EntityType<? extends LivingEntity>) entry.getKey(), renderer, new RegistrationHelperImpl(accessor::callAddFeature), ctx);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "reloadPlayerRenderers", at = @At(value = "RETURN"))
    private static void createPlayerEntityRenderer(EntityRendererFactory.Context ctx, CallbackInfoReturnable<Map<String, EntityRenderer<? extends PlayerEntity>>> cir) {
        for (EntityRenderer<? extends PlayerEntity> entityRenderer : cir.getReturnValue().values()) {
            LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor) entityRenderer;
            LivingEntityFeatureRenderEvent.EVENT.invoker().registerRenderers(EntityType.PLAYER, (LivingEntityRenderer) entityRenderer, new RegistrationHelperImpl(accessor::callAddFeature), ctx);
        }
    }
}
