package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.client.ClientTriggerHandler;
import com.imoonday.advskills_re.trigger.SkillTriggerHandler;
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
    private void advskills_re$tick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        ClientTriggerHandler.sendPlayerData(player);
    }

    @ModifyArg(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"), index = 1)
    private double advskills_re$tickMovement(double y) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            if (SkillTriggerHandler.shouldInvertJump(player) && y > 0 || SkillTriggerHandler.shouldInvertSneak(player) && y < 0) {
                return -y;
            }
        }
        return y;
    }
}
