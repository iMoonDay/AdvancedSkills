package com.imoonday.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public class FluidBlockMixin {

    @Unique
    private static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

    @Shadow
    @Final
    public static IntProperty LEVEL;

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (context instanceof EntityShapeContext entityShapeContext && entityShapeContext.getEntity() instanceof PlayerEntity && context.isAbove(COLLISION_SHAPE, pos, true) && state.get(LEVEL) == 0 && context.canWalkOnFluid(world.getFluidState(pos.up()), state.getFluidState())) {
            cir.setReturnValue(COLLISION_SHAPE);
        }
    }
}
