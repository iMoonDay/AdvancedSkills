package com.imoonday.advanced_skills_re.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface AllowDeathEvent {

    Event<AllowDeathEvent> EVENT = EventFactory.createEventResult();

    EventResult allowDeath(ServerPlayerEntity player, DamageSource source, float amount);
}
