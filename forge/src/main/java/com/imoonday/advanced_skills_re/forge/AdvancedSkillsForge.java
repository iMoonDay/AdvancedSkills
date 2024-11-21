package com.imoonday.advanced_skills_re.forge;

import com.imoonday.AdvancedSkills;
import com.imoonday.AdvancedSkillsKt;
import com.imoonday.advanced_skills_re.api.WorldRenderEvents;
import com.imoonday.advanced_skills_re.forge.api.WorldRenderContextForgeImpl;
import com.imoonday.client.AdvancedSkillsClient;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Arrays;
import java.util.List;

@Mod(AdvancedSkillsKt.MOD_ID)
public final class AdvancedSkillsForge {

    public AdvancedSkillsForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(AdvancedSkillsKt.MOD_ID, bus);
        AdvancedSkills.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AdvancedSkillsClient::initClient);
        bus.addListener(this::modifyKeyConflicts);

        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addListener(this::onRenderLevelStage);
    }

    private void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            WorldRenderEvents.AFTER_ENTITIES.invoker().afterEntities(WorldRenderContextForgeImpl.of(event));
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            WorldRenderEvents.LAST.invoker().last(WorldRenderContextForgeImpl.of(event));
        }
    }

    private void modifyKeyConflicts(FMLClientSetupEvent event) {
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
}
