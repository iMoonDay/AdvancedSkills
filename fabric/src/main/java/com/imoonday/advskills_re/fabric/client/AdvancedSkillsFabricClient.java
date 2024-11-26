package com.imoonday.advskills_re.fabric.client;

import com.imoonday.advskills_re.api.LivingEntityFeatureRenderEvent;
import com.imoonday.advskills_re.client.AdvancedSkillsClient;
import com.imoonday.advskills_re.fabric.api.WorldRenderContextFabricImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public final class AdvancedSkillsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AdvancedSkillsClient.initClient();
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> LivingEntityFeatureRenderEvent.EVENT.invoker().registerRenderers(entityType, entityRenderer, registrationHelper::register, context));
        WorldRenderEvents.AFTER_ENTITIES.register(context -> com.imoonday.advskills_re.api.WorldRenderEvents.AFTER_ENTITIES.invoker().afterEntities(WorldRenderContextFabricImpl.of(context)));
        WorldRenderEvents.LAST.register(context -> com.imoonday.advskills_re.api.WorldRenderEvents.LAST.invoker().last(WorldRenderContextFabricImpl.of(context)));
    }
}
