package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.client.ClientTriggerHandler;
import com.imoonday.advskills_re.trigger.SkillTriggerHandler;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Mouse.class)
public class MouseMixin {

    @ModifyArg(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), index = 0)
    private double advskills_re$updateMouse$1(double x) {
        return ClientTriggerHandler.INSTANCE.shouldInvertMouse().getFirst() ? x * -1 : x;
    }

    @ModifyArg(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), index = 1)
    private double advskills_re$updateMouse$2(double y) {
        return ClientTriggerHandler.INSTANCE.shouldInvertMouse().getSecond() ? y * -1 : y;
    }
}
