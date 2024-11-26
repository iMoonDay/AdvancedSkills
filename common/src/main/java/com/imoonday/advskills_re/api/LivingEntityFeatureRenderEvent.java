package com.imoonday.advskills_re.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;

/**
 * From Fabric Api
 */
public interface LivingEntityFeatureRenderEvent {

    Event<LivingEntityFeatureRenderEvent> EVENT = EventFactory.createLoop();

    /**
     * Called when feature renderers may be registered.
     *
     * @param entityType     the entity type of the renderer
     * @param entityRenderer the entity renderer
     */
    void registerRenderers(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?> entityRenderer, RegistrationHelper registrationHelper, EntityRendererFactory.Context context);

    /**
     * A delegate object used to help register feature renderers for an entity renderer.
     *
     * <p>This is not meant for implementation by users of the API.
     */
    @ApiStatus.NonExtendable
    interface RegistrationHelper {

        /**
         * Adds a feature renderer to the entity renderer.
         *
         * @param featureRenderer the feature renderer
         * @param <T>             the type of entity
         */
        <T extends LivingEntity> void register(FeatureRenderer<T, ? extends EntityModel<T>> featureRenderer);
    }
}
