package com.imoonday.advskills_re.fabric;

import com.imoonday.advskills_re.AdvancedSkills;
import com.imoonday.advskills_re.api.AllowDeathEvent;
import com.imoonday.advskills_re.api.DataPackReloadEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public final class AdvancedSkillsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        AdvancedSkills.init();
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> !(entity instanceof ServerPlayerEntity player) || AllowDeathEvent.EVENT.invoker().allowDeath(player, damageSource, damageAmount));
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> DataPackReloadEvents.START.invoker().startDataPackReload(server, resourceManager));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> DataPackReloadEvents.END.invoker().endDataPackReload(server, resourceManager, success));
    }
}
