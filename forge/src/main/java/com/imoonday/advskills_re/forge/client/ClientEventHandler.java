package com.imoonday.advskills_re.forge.client;

import com.imoonday.advskills_re.api.WorldRenderEvents;
import com.imoonday.advskills_re.forge.api.WorldRenderContextForgeImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Arrays;
import java.util.List;

public class ClientEventHandler {

    @SubscribeEvent
    public static void modifyKeyConflicts(FMLClientSetupEvent event) {
        GameOptions options = MinecraftClient.getInstance().options;
        List<KeyBinding> keys = Arrays.asList(options.forwardKey,
                                              options.leftKey,
                                              options.backKey,
                                              options.rightKey,
                                              options.jumpKey,
                                              options.sneakKey,
                                              options.sprintKey);
        for (KeyBinding key : keys) {
            key.setKeyConflictContext(KeyConflictContext.UNIVERSAL);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            WorldRenderEvents.AFTER_ENTITIES.invoker().afterEntities(WorldRenderContextForgeImpl.of(event));
        }
    }

    public static void registerConfigScreenFactory() {
        FMLJavaModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> com.imoonday.advskills_re.client.screen.ConfigScreenHandler.createScreen(parent)
                )
        );
    }
}
