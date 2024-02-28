package com.imoonday.entities

import net.minecraft.entity.Ownable
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.*

interface Servant : Ownable {
    var ownerUuid: UUID?

    companion object {
        fun invulnerableToServant(
            damageSource: DamageSource,
            cir: CallbackInfoReturnable<Boolean?>,
            player: PlayerEntity,
        ) {
            val attacker = damageSource.attacker
            val source = damageSource.source
            if (attacker is Servant && player.uuid == attacker.ownerUuid) {
                cir.returnValue = true
            }
            if (source is Servant && player.uuid == source.ownerUuid) {
                cir.returnValue = true
            }
        }
    }
}