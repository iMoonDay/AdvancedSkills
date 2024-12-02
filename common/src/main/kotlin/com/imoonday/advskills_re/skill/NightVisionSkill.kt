package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*

class NightVisionSkill : PassiveSkill(
    id = "night_vision",
    rarity = Rarity.SUPERB,
    toggleable = true
), PersistentTrigger, NightVisionTrigger