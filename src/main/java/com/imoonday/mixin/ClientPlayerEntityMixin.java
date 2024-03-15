package com.imoonday.mixin;

import com.imoonday.trigger.SkillTriggerHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void advanced_skills$tick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        SkillTriggerHandler.INSTANCE.sendPlayerData(player);
    }

    @ModifyArg(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"), index = 1)
    private double advanced_skills$tickMovement(double y) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            if (SkillTriggerHandler.INSTANCE.shouldInvertJump(player) && y > 0 || SkillTriggerHandler.INSTANCE.shouldInvertSneak(player) && y < 0) {
                return -y;
            }
        }
        return y;
    }
}
