package com.imoonday.advskills_re.mixin;

import net.minecraft.data.server.loottable.BlockLootTableGenerator;
import net.minecraft.loot.condition.LootCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockLootTableGenerator.class)
public interface BlockLootTableGeneratorAccessor {

    @Accessor("WITHOUT_SILK_TOUCH_NOR_SHEARS")
    static LootCondition.Builder getWithoutSilkTouchNorShearsBuilder() {
        throw new UnsupportedOperationException();
    }
}
