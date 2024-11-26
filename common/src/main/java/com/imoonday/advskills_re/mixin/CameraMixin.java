package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.client.ClientTriggerHandler;
import com.imoonday.advskills_re.trigger.SkillTriggerHandler;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow private float lastCameraY;

    @ModifyConstant(method = "updateEyeHeight", constant = @Constant(floatValue = 0.5f))
    private float advskills_re$updateEyeHeight(float constant) {
        return ClientTriggerHandler.INSTANCE.getCameraMovement(constant);
    }
}
