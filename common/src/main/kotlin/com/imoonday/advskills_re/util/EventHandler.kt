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
                .forEach { player.stopAndCooldown(it) }
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
        PlayerEvent.PLAYER_JOIN.register { player ->
            Channels.SYNC_CONFIG_S2C.sendToPlayer(
                player, SyncConfigS2CPacket(
                    SkillConfig.get().writeToNbt(GlobalConfig.get().toNbt()),
                    SyncConfigS2CPacket.ConfigType.BOTH
                )
            )
            Channels.SYNC_RARITIES_S2C.sendToPlayer(player, SyncRaritiesS2CPacket(RarityManager.getLoadedRarities()))
            Channels.SYNC_SETTINGS_S2C.sendToPlayer(
                player,
                SyncSettingsS2CPacket(Skills.getSkills().map { it.settings })
            )
        }
        LifecycleEvent.SERVER_STARTED.register {
            SkillConfig.init(it)
        }
        LifecycleEvent.SERVER_STOPPING.register {
            SkillConfig.get().save()
        }
        LifecycleEvent.SERVER_STOPPED.register {
            SkillConfig.get().reset()
            SkillConfig.resetFile()
        }
    }

    private fun registerLootTables() {
        val fishingLootTable = LootTables.FISHING_TREASURE_GAMEPLAY

        LootEvent.MODIFY_LOOT_TABLE.register { _, id, context, builtin ->
            val config = GlobalConfig.get()
            if (!config.disableSkillFruitGeneration && builtin) {
                val blockLootTables = mapOf(
                    Blocks.OAK_LEAVES.lootTableId to config.oakLeavesDropChance,
                    Blocks.DARK_OAK_LEAVES.lootTableId to config.darkOakLeavesDropChance
                )
                val chestLootTables = mapOf(
                    LootTables.ANCIENT_CITY_CHEST to config.ancientCityChestGenerationChance,
                    LootTables.BURIED_TREASURE_CHEST to config.buriedTreasureChestGenerationChance,
                    LootTables.END_CITY_TREASURE_CHEST to config.endCityTreasureChestGenerationChance
                )
                val spawnBonusChest = LootTables.SPAWN_BONUS_CHEST to config.spawnBonusChestGenerationChance

                val builder = {
                    var builder = LootPool.builder()
                    ModItems.FRUITS.forEach {
                        val item = it.get()
                        builder = builder.with(ItemEntry.builder(item).weight(item.rarity.weight))
                    }
                    builder
                }

                when {
                    blockLootTables.containsKey(id) -> context.addPool(
                        builder().apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.FORTUNE))
                            .conditionally(SurvivesExplosionLootCondition.builder())
                            .conditionally(BlockLootTableGeneratorAccessor.getWithoutSilkTouchNorShearsBuilder())
                            .conditionally(RandomChanceLootCondition.builder(blockLootTables[id]!!))
                    )

                    id == fishingLootTable -> context.addPool(builder())

                    chestLootTables.containsKey(id) -> context.addPool(
                        builder().conditionally(RandomChanceLootCondition.builder(chestLootTables[id]!!))
                    )

                    id == spawnBonusChest.first -> context.addPool(
                        builder().rolls(UniformLootNumberProvider.create(1f, 6f))
                            .conditionally(RandomChanceLootCondition.builder(spawnBonusChest.second))
                    )
                }
            }
        }
    }
}