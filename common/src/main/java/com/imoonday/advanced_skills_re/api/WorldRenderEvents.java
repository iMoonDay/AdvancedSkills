package com.imoonday.advanced_skills_re.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

public interface WorldRenderEvents {

    Event<AfterEntities> AFTER_ENTITIES = EventFactory.createLoop();
    Event<Last> LAST = EventFactory.createLoop();

    interface AfterEntities {

        void afterEntities(WorldRenderContext context);
    }

    interface Last {

        void last(WorldRenderContext context);
    }
}
