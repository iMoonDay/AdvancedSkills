package com.imoonday.advskills_re.fabric;

import com.imoonday.advskills_re.AdvancedSkills;
import com.imoonday.advskills_re.api.AllowDeathEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public final class AdvancedSkillsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        AdvancedSkills.init();
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> !(entity instanceof ServerPlayerEntity player) || AllowDeathEvent.EVENT.invoker().allowDeath(player, damageSource, damageAmount));
    }
}
