package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.skill.trigger.SkillTriggerHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    private boolean floating;

    @Redirect(method = "onPlayerMove", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;floating:Z", opcode = Opcodes.PUTFIELD))
    private void setFloating(ServerPlayNetworkHandler serverPlayNetworkHandler, boolean floating) {
        if (floating && SkillTriggerHandler.isSaveMoving(serverPlayNetworkHandler.player)) {
            this.floating = false;
        }
    }
}
