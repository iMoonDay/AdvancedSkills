package com.imoonday.advskills_re.forge.mixin;

import com.imoonday.advskills_re.api.DataPackReloadEvents;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    private MinecraftServer.ResourceManagerHolder resourceManagerHolder;

    @Inject(method = "reloadResources", at = @At("HEAD"))
    private void startResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        DataPackReloadEvents.START.invoker().startDataPackReload((MinecraftServer) (Object) this, this.resourceManagerHolder.resourceManager());
    }

    @Inject(method = "reloadResources", at = @At("TAIL"))
    private void endResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        cir.getReturnValue().handleAsync((value, throwable) -> {
            // Hook into fail
            DataPackReloadEvents.END.invoker().endDataPackReload((MinecraftServer) (Object) this, this.resourceManagerHolder.resourceManager(), throwable == null);
            return value;
        }, (MinecraftServer) (Object) this);
    }
}
