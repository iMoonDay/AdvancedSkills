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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Bug:
 * 1.强健体魄死后失效 √
 * 2.洞察之眼看不到复活后玩家的技能，全部为空
 * 3.死亡时触发技能失效 √
 * 4.矿物感知渲染错误 √
 * 5.液体护盾水中无法奔跑 √
 * 6.冰霜陷阱会冰冻自己 √
 */
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
        });
    }
}
