package com.imoonday.mixin;

import com.imoonday.trigger.SkillTriggerHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.HeartType.class)
public class HeartTypeMixin {

    @ModifyReturnValue(method = "fromPlayerState", at = @At("RETURN"))
    private static InGameHud.HeartType advanced_skills$fromPlayerState(InGameHud.HeartType original, PlayerEntity player) {
        InGameHud.HeartType type = SkillTriggerHandler.INSTANCE.getHeartType(player);
        return type != null ? type : original;
    }
}
