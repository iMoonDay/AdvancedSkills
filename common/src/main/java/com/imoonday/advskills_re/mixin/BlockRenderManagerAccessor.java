package com.imoonday.advskills_re.mixin;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockRenderManager.class)
public interface BlockRenderManagerAccessor {

    @Accessor
    BlockColors getBlockColors();

    @Accessor
    BlockModelRenderer getBlockModelRenderer();

    @Accessor
    BuiltinModelItemRenderer getBuiltinModelItemRenderer();
}
