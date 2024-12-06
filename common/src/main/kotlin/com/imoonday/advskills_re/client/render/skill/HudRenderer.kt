package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.trigger.client.render.*

interface HudRenderer<T> : IHudRenderer<T> where T : Skill, T : HudRenderTrigger