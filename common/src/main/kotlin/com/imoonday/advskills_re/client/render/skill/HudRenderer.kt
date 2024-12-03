package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.trigger.renderer.*

interface HudRenderer<T> : IHudRenderer<T> where T : Skill, T : HudRenderTrigger