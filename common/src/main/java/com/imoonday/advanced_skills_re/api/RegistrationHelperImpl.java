package com.imoonday.advanced_skills_re.api;

import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;

import java.util.Objects;
import java.util.function.Function;

public final class RegistrationHelperImpl implements LivingEntityFeatureRenderEvent.RegistrationHelper {

    private final Function<FeatureRenderer<?, ?>, Boolean> delegate;

    public RegistrationHelperImpl(Function<FeatureRenderer<?, ?>, Boolean> delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T extends LivingEntity> void register(FeatureRenderer<T, ? extends EntityModel<T>> featureRenderer) {
        Objects.requireNonNull(featureRenderer, "Feature renderer cannot be null");
        this.delegate.apply(featureRenderer);
    }
}
