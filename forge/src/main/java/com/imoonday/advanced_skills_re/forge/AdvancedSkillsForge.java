package com.imoonday.advanced_skills_re.forge;

import com.imoonday.AdvancedSkills;
import com.imoonday.AdvancedSkillsKt;
import com.imoonday.advanced_skills_re.api.WorldRenderEvents;
import com.imoonday.advanced_skills_re.forge.api.WorldRenderContextForgeImpl;
import com.imoonday.client.AdvancedSkillsClient;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AdvancedSkillsKt.MOD_ID)
public final class AdvancedSkillsForge {

    public AdvancedSkillsForge(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        AdvancedSkills.INSTANCE.init();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        AdvancedSkillsClient.INSTANCE.initClient();
    }

    private void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            WorldRenderEvents.AFTER_ENTITIES.invoker().afterEntities(WorldRenderContextForgeImpl.of(event));
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            WorldRenderEvents.LAST.invoker().last(WorldRenderContextForgeImpl.of(event));
        }
    }
}
