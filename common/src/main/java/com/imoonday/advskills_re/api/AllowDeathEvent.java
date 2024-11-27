package com.imoonday.advskills_re.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface AllowDeathEvent {

    Event<AllowDeathEvent> EVENT = EventFactory.createEventResult();

    Boolean allowDeath(ServerPlayerEntity player, DamageSource source, float amount);
}
