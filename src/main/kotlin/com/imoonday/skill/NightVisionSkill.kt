package com.imoonday.skill

import com.imoonday.trigger.PersistentTrigger
import com.imoonday.util.SkillType

class NightVisionSkill : PassiveSkill(
    id = "night_vision",
    types = arrayOf(SkillType.PASSIVE),
    rarity = Rarity.SUPERB,
    toggleable = true
), PersistentTrigger