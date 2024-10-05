package com.imoonday.advanced_skills_re.mixin;

import com.imoonday.trigger.SkillTriggerHandler;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Mouse.class)
public class MouseMixin {

    @ModifyArg(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), index = 0)
    private double advanced_skills$updateMouse$1(double x) {
        return SkillTriggerHandler.INSTANCE.shouldInvertMouse().getFirst() ? x * -1 : x;
    }

    @ModifyArg(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), index = 1)
    private double advanced_skills$updateMouse$2(double y) {
        return SkillTriggerHandler.INSTANCE.shouldInvertMouse().getSecond() ? y * -1 : y;
    }
}
