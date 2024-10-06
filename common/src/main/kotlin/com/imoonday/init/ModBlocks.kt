package com.imoonday.init

import com.imoonday.*
import com.imoonday.block.*
import com.imoonday.block.entity.*
import com.imoonday.util.*
import dev.architectury.registry.registries.*
import net.minecraft.block.*
import net.minecraft.block.entity.*
import net.minecraft.block.entity.BlockEntityType.*
import net.minecraft.datafixer.*
import net.minecraft.registry.*
import net.minecraft.sound.*
import net.minecraft.util.*
import java.util.function.*

object ModBlocks {

    @JvmField
    val BLOCKS = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK)

    @JvmField
    val BLOCK_ENTITIES = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE)

    @JvmField
    val INVISIBLE_TRAP = InvisibleTrapBlock(
        AbstractBlock.Settings.create()
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
        AbstractBlock.Settings.create()
            .sounds(BlockSoundGroup.SNOW)
            .replaceable()
            .breakInstantly()
            .noCollision()
    ).register("frost_trap")

    @JvmField
    val FROST_TRAP_ENTITY = registerEntity("frost_trap", ::InvisibleTrapBlockEntity, FROST_TRAP)

    fun <T : Block> T.register(id: String): RegistrySupplier<T> = BLOCKS.register(id) { this }

    fun <T : BlockEntity> registerEntity(
        id: String,
        factory: BlockEntityFactory<T>,
        vararg blocks: Supplier<out Block>,
    ): RegistrySupplier<BlockEntityType<T>> = BLOCK_ENTITIES.register(id) {
        Builder.create(factory, *blocks.map(Supplier<out Block>::get).toTypedArray()).build(
            Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id)
        )
    }

    fun init() {
        BLOCKS.register()
        BLOCK_ENTITIES.register()
    }
}