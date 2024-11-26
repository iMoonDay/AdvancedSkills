package com.imoonday.advskills_re.init

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.block.*
import com.imoonday.advskills_re.block.entity.*
import com.imoonday.advskills_re.util.*
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
    val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK)

    @JvmField
    val BLOCK_ENTITIES: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE)

    @JvmField
    val INVISIBLE_TRAP: RegistrySupplier<InvisibleTrapBlock> = BLOCKS.register("invisible_trap") {
        InvisibleTrapBlock(
            AbstractBlock.Settings.create()
                .dropsNothing()
                .noCollision()
                .breakInstantly()
                .replaceable()
                .sounds(BlockSoundGroup.GLASS)
                .noBlockBreakParticles()
        )
    }

    @JvmField
    val INVISIBLE_TRAP_ENTITY = registerEntity("invisible_trap", ::InvisibleTrapBlockEntity, INVISIBLE_TRAP)

    @JvmField
    val FROST_TRAP: RegistrySupplier<FrostTrapBlock> = BLOCKS.register("frost_trap") {
        FrostTrapBlock(
            AbstractBlock.Settings.create()
                .sounds(BlockSoundGroup.SNOW)
                .replaceable()
                .breakInstantly()
                .noCollision()
        )
    }

    @JvmField
    val FROST_TRAP_ENTITY = registerEntity("frost_trap", ::InvisibleTrapBlockEntity, FROST_TRAP)

    private fun <T : BlockEntity> registerEntity(
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