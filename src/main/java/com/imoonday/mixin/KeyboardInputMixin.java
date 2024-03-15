package com.imoonday.mixin;

import com.imoonday.trigger.SkillTriggerHandler;
import kotlin.Pair;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/KeyboardInput;getMovementMultiplier(ZZ)F", ordinal = 0))
    private void advanced_skills$tick(CallbackInfo ci) {
        Pair<Boolean, Boolean> invertInput = SkillTriggerHandler.INSTANCE.shouldInvertInput();
        if (invertInput.getFirst()) {
            boolean temp = this.pressingLeft;
            this.pressingLeft = this.pressingRight;
            this.pressingRight = temp;
        }
        if (invertInput.getSecond()) {
            boolean temp = this.pressingForward;
            this.pressingForward = this.pressingBack;
            this.pressingBack = temp;
        }
    }
}
