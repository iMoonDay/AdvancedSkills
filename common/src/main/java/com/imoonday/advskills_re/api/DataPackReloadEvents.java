package com.imoonday.advskills_re.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;

public class DataPackReloadEvents {

    public static final Event<StartDataPackReload> START = EventFactory.createLoop();
    public static final Event<EndDataPackReload> END = EventFactory.createLoop();

    @FunctionalInterface
    public interface StartDataPackReload {

        void startDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager);
    }

    @FunctionalInterface
    public interface EndDataPackReload {

        void endDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success);
    }
}
