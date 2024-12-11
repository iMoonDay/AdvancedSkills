package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.mixin.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.skill.trigger.*
import dev.architectury.event.*
import dev.architectury.event.events.common.*
import net.minecraft.block.*
import net.minecraft.enchantment.*
import net.minecraft.loot.*
import net.minecraft.loot.condition.*
import net.minecraft.loot.entry.*
import net.minecraft.loot.function.*
import net.minecraft.loot.provider.number.*
import net.minecraft.nbt.*
import net.minecraft.registry.tag.*
import net.minecraft.server.network.*

object EventHandler {

    fun register() {
        SkillChangeEvents.EQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.onEquipped(player, slot, skill).toEventResult()
        }
        SkillChangeEvents.UNEQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.onUnequipped(player, slot, skill).toEventResult()
        }
        SkillChangeEvents.POST_EQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.postEquipped(player, slot, skill)
            SkillTriggerHandler.onStart(player, skill)
        }
        SkillChangeEvents.POST_UNEQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.postUnequipped(player, slot, skill)
            player.stopUsing(skill)
        }
        PlayerEvent.PLAYER_CLONE.register { oldPlayer, newPlayer, _ ->
            newPlayer.copyDataFrom(oldPlayer)
            newPlayer.properties.replaceAll(oldPlayer.properties)
            newPlayer.syncData(false)
            newPlayer.syncProperties()
        }
        AllowDeathEvent.EVENT.register { player, source, amount ->
            source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || player.getTriggers<DeathTrigger>().none {
                !it.allowDeath(player, source, amount)
            }
        }
        EntityEvent.LIVING_DEATH.register { entity, source ->
            if (entity is ServerPlayerEntity) {
                entity.forEachTrigger<DeathTrigger> { it.onDeath(entity, source) }
            }
            EventResult.pass()
        }
        PlayerEvent.PLAYER_RESPAWN.register { player, _ ->
            player.usingSkills
                .filterNot { it is RespawnTrigger && it.keepUsingAfterRespawn(player) }
                .forEach { player.stopUsing(it) }
            player.forEachTrigger<RespawnTrigger> { it.afterRespawn(player) }
        }
        PlayerEvent.ATTACK_ENTITY.register { player, _, _, _, _ ->
            if (player.isDisarmed) EventResult.interruptFalse()
            else EventResult.pass()
        }
        InteractionEvent.RIGHT_CLICK_ITEM.register { player, hand ->
            val stack = player.getStackInHand(hand)
            if (player.isDisarmed) CompoundEventResult.interruptFalse(stack)
            else CompoundEventResult.pass()
        }
        registerLootTables()
        PlayerEvent.PLAYER_JOIN.register {
            Channels.SYNC_CONFIG_S2C.sendToPlayer(it, SyncConfigS2CPacket(SkillConfig.get().save(NbtCompound())))
        }
        LifecycleEvent.SERVER_BEFORE_START.register {
            ServerConfig.init(it)
        }
        LifecycleEvent.SERVER_STARTED.register {
            SkillConfig.get().connectToServer(it)
        }
        LifecycleEvent.SERVER_STOPPING.register {
            SkillConfig.get().markDirty()
        }
        LifecycleEvent.SERVER_STOPPED.register {
            SkillConfig.get().disconnect()
        }
    }

    private fun registerLootTables() {
        val blockLootTables = mapOf(
            Blocks.OAK_LEAVES.lootTableId to 0.005f,
            Blocks.DARK_OAK_LEAVES.lootTableId to 0.005f
        )
        val fishingLootTables = listOf(
            LootTables.FISHING_TREASURE_GAMEPLAY
        )
        val chestLootTables = mapOf(
            LootTables.ANCIENT_CITY_CHEST to 0.25f,
            LootTables.BURIED_TREASURE_CHEST to 0.25f,
            LootTables.END_CITY_TREASURE_CHEST to 0.25f
        )
        val spawnBonusChest = LootTables.SPAWN_BONUS_CHEST
        val builder = {
            var builder = LootPool.builder()
            ModItems.FRUITS.forEach {
                val item = it.get()
                builder = builder.with(ItemEntry.builder(item).weight(item.rarity.weight))
            }
            builder
        }
        LootEvent.MODIFY_LOOT_TABLE.register { _, id, context, builtin ->
            if (!ServerConfig.get().disableSkillFruitGeneration && builtin) {
                when {
                    blockLootTables.containsKey(id) -> context.addPool(
                        builder().apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.FORTUNE))
                            .conditionally(SurvivesExplosionLootCondition.builder())
                            .conditionally(BlockLootTableGeneratorAccessor.getWithoutSilkTouchNorShearsBuilder())
                            .conditionally(RandomChanceLootCondition.builder(blockLootTables[id]!!))
                    )

                    fishingLootTables.contains(id) -> context.addPool(builder())

                    chestLootTables.containsKey(id) -> context.addPool(
                        builder().conditionally(RandomChanceLootCondition.builder(chestLootTables[id]!!))
                    )

                    id == spawnBonusChest -> context.addPool(builder().rolls(UniformLootNumberProvider.create(1f, 6f)))
                }
            }
        }
    }
}