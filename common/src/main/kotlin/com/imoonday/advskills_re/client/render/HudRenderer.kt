package com.imoonday.advskills_re.client.render

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.trigger.*

interface HudRenderer<T> : IHudRenderer<T> where T : Skill, T : HudRenderTrigger