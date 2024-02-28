package com.imoonday.skills

import com.imoonday.components.toggleUsingSkill
import com.imoonday.triggers.PersistentTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import com.imoonday.utils.translateSkill
import net.minecraft.server.network.ServerPlayerEntity

class NightVisionSkill : Skill(
    id = "night_vision",
    types = arrayOf(SkillType.PASSIVE),
    cooldown = 0,
    rarity = Rarity.VERY_RARE,
), PersistentTrigger {
    override fun use(user: ServerPlayerEntity): UseResult {
//        if (user.getStatusEffect(StatusEffects.NIGHT_VISION)?.isInfinite == false) return UseResult.fail(
//            translate("useSkill", "active", name.string)
//        )
//        val active = if (user.isUsingSkill(this)) {
//            user.stopUsingSkill(this)
//            if (user.getStatusEffect(StatusEffects.NIGHT_VISION)?.isInfinite == true) user.removeStatusEffect(
//                StatusEffects.NIGHT_VISION
//            )
//            false
//        } else {
//            user.startUsingSkill(this)
//            user.addStatusEffect(StatusEffectInstance(StatusEffects.NIGHT_VISION, -1))
//            true
//        }
        val active = user.toggleUsingSkill(this)
        return UseResult.consume(
            translateSkill(
                "wall_climbing", if (active) "active" else "inactive",
                name.string
            )
        )
    }

//    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
//        player.stopUsingSkill(this)
//        if (player.getStatusEffect(StatusEffects.NIGHT_VISION)?.isInfinite == true) player.removeStatusEffect(
//            StatusEffects.NIGHT_VISION
//        )
//    }
}