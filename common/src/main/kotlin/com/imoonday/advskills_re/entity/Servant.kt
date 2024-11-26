package com.imoonday.advskills_re.entity

import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import org.spongepowered.asm.mixin.injection.callback.*
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