package com.imoonday.advanced_skills_re.mixin;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {

    @Accessor
    BufferBuilderStorage getBufferBuilders();

    @Accessor
    PostEffectProcessor getTransparencyPostProcessor();

    @Accessor
    ClientWorld getWorld();
}
