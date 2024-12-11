package com.imoonday.advskills_re.forge;

import com.imoonday.advskills_re.AdvancedSkills;
import com.imoonday.advskills_re.AdvancedSkillsKt;
import com.imoonday.advskills_re.client.AdvancedSkillsClient;
import com.imoonday.advskills_re.forge.client.ClientEventHandler;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AdvancedSkillsKt.MOD_ID)
public final class AdvancedSkillsForge {

    public AdvancedSkillsForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        EventBuses.registerModEventBus(AdvancedSkillsKt.MOD_ID, bus);
        AdvancedSkills.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AdvancedSkillsClient::initClient);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(ClientEventHandler::modifyKeyConflicts);
            forgeEventBus.addListener(ClientEventHandler::onRenderLevelStage);
            if (AdvancedSkillsClient.getClothConfigLoaded()) {
                ClientEventHandler.registerConfigScreenFactory();
            }
        });
    }
}
