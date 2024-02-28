package com.imoonday.mixin;

import com.imoonday.components.SkillExpComponentKt;
import com.imoonday.entities.Servant;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "addExperience", at = @At("TAIL"))
    public void advanced_skills$addExperience(int experience, CallbackInfo ci) {
        if (experience > 0) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            SkillExpComponentKt.setSkillExp(player, SkillExpComponentKt.getSkillExp(player) + experience);
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$isInvulnerableTo(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        Servant.Companion.invulnerableToServant(damageSource, cir, player);
    }
}
