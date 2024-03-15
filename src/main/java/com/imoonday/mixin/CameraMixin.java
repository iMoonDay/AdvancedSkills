package com.imoonday.mixin;

import com.imoonday.trigger.SkillTriggerHandler;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow private float lastCameraY;

    @ModifyConstant(method = "updateEyeHeight", constant = @Constant(floatValue = 0.5f))
    private float advanced_skills$updateEyeHeight(float constant) {
        return SkillTriggerHandler.INSTANCE.getCameraMovement(constant);
    }
}
