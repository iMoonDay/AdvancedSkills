package com.imoonday.advskills_re.entity

import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.mob.*
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.*
import org.spongepowered.asm.mixin.injection.callback.*
import java.util.*

interface Servant : Ownable {

    var ownerUuid: UUID?

    fun canAttackWithOwner(target: LivingEntity?, owner: LivingEntity): Boolean =
        if (target is CreeperEntity || target is GhastEntity) {
            false
        } else if (target is Servant) {
            target.owner !== owner
        } else if (target is PlayerEntity && owner is PlayerEntity && !owner.shouldDamagePlayer(target)) {
            false
        } else {
            if (target is AbstractHorseEntity && target.isTame) false
            else target !is TameableEntity || !target.isTamed
        }

    companion object {

        @JvmStatic
        fun invulnerableToServant(
            damageSource: DamageSource,
            player: PlayerEntity,
            cir: CallbackInfoReturnable<Boolean>,
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