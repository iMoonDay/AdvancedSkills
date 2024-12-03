package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.text.*

fun translate(key: String, vararg args: Any): MutableText =
    Text.translatable("$MOD_ID.$key", *args)

fun translateSkill(id: String, key: String, vararg args: Any): MutableText = translate("skill.$id.$key", *args)

fun translateActive(skill: Skill, active: Boolean): MutableText =
    translate("useSkill.${if (active) "active" else "inactive"}", skill.name)