package com.imoonday.mixin;

import com.imoonday.trigger.SkillTriggerHandler;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "onLanding", at = @At("HEAD"))
    public void advanced_skills$onLanding(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof ServerPlayerEntity player) {
            SkillTriggerHandler.INSTANCE.onLanding(player, player.fallDistance);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void advanced_skills$tick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        SkillTriggerHandler.INSTANCE.tick(player);
    }
}
