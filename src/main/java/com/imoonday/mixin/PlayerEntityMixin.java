package com.imoonday.mixin;

import com.imoonday.components.SkillExpComponentKt;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "addExperience", at = @At("TAIL"))
    public void advanced_skills$addExperience(int experience, CallbackInfo ci) {
        if (experience > 0) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            SkillExpComponentKt.setSkillExp(player, SkillExpComponentKt.getSkillExp(player) + experience);
        }
    }
}
