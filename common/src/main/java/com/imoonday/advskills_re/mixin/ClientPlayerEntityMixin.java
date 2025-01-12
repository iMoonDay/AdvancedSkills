package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.client.ClientTriggerHandler;
import com.imoonday.advskills_re.network.Channels;
import com.imoonday.advskills_re.network.c2s.UpdateJumpingC2SPacket;
import com.imoonday.advskills_re.skill.trigger.SkillTriggerHandler;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow
    public Input input;
    @Unique
    private boolean advskills_re$wasJumping;
    @Unique
    private boolean advskills_re$onGround;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
        throw new UnsupportedOperationException("Mixin Constructor");
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void advskills_re$onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        this.advskills_re$onGround = player.isOnGround();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void advskills_re$postTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        ClientTriggerHandler.sendPlayerData(player);
    }

    @ModifyArg(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"), index = 1)
    private double advskills_re$tickMovement(double y) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (SkillTriggerHandler.shouldInvertJump(player) && y > 0 || SkillTriggerHandler.shouldInvertSneak(player) && y < 0) {
            return -y;
        }
        return y;
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void advskills_re$sendMovementPackets(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        boolean jumping = this.input.jumping;
        if (ClientTriggerHandler.shouldSyncJumpState(player)) {
            if (jumping != this.advskills_re$wasJumping) {
                if (jumping) {
                    SkillTriggerHandler.onJumped(player, this.advskills_re$onGround);
                }
                Channels.getUPDATE_JUMPING_C2S().sendToServer(new UpdateJumpingC2SPacket(jumping));
            }
        }

        this.advskills_re$wasJumping = jumping;
    }
}
