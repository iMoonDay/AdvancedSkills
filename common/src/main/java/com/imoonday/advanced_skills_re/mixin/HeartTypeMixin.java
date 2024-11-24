package com.imoonday.advanced_skills_re.mixin;

import com.imoonday.client.ClientTriggerHandler;
import com.imoonday.trigger.SkillTriggerHandler;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InGameHud.HeartType.class)
public class HeartTypeMixin {

    @Inject(method = "fromPlayerState", at = @At("RETURN"), cancellable = true)
    private static void advanced_skills_re$fromPlayerState(PlayerEntity player, CallbackInfoReturnable<InGameHud.HeartType> cir) {
        InGameHud.HeartType type = ClientTriggerHandler.INSTANCE.getHeartType(player);
        if (type != null) {
            cir.setReturnValue(type);
        }
    }
}
