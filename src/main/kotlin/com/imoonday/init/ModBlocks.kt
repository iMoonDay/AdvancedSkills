package com.imoonday.init

import com.imoonday.block.FrostTrapBlock
import com.imoonday.block.InvisibleTrapBlock
import com.imoonday.block.entity.InvisibleTrapBlockEntity
import com.imoonday.util.id
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.BlockEntityType.BlockEntityFactory
import net.minecraft.datafixer.TypeReferences
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Util

object ModBlocks {

    @JvmField
    val INVISIBLE_TRAP = InvisibleTrapBlock(
        FabricBlockSettings.create()
            .dropsNothing()
            .noCollision()
            .breakInstantly()
            .replaceable()
            .sounds(BlockSoundGroup.GLASS)
            .noBlockBreakParticles()
    ).register("invisible_trap")

    @JvmField
    val INVISIBLE_TRAP_ENTITY = registerEntity("invisible_trap", ::InvisibleTrapBlockEntity, INVISIBLE_TRAP)

    @JvmField
    val FROST_TRAP = FrostTrapBlock(
        FabricBlockSettings.create()
            .sounds(BlockSoundGroup.SNOW)
            .replaceable()
            .breakInstantly()
            .noCollision()
    ).register("frost_trap")

    @JvmField
    val FROST_TRAP_ENTITY = registerEntity("frost_trap", ::InvisibleTrapBlockEntity, FROST_TRAP)

    fun <T : Block> T.register(id: String): T {
        return Registry.register(Registries.BLOCK, id(id), this)
    }

    fun <T : BlockEntity> registerEntity(
        id: String,
        factory: BlockEntityFactory<T>,
        vararg blocks: Block,
    ): BlockEntityType<T> = Registry.register(
        Registries.BLOCK_ENTITY_TYPE, id(id), BlockEntityType.Builder.create(factory, *blocks).build(
            Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id)
        )
    )

    fun init() = Unit
}