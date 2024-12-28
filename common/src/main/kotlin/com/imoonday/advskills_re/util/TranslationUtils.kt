package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.text.*

fun translate(key: String, vararg args: Any): MutableText =
    Text.translatable("$MOD_ID.$key", *args)

fun translateKey(key: String): String = "$MOD_ID.$key"

fun translateSkill(id: String, key: String, vararg args: Any): MutableText = translate("skill.$id.$key", *args)

fun translateSkillKey(id: String, key: String): String = "$MOD_ID.skill.$id.$key"

fun translateActive(skill: Skill, active: Boolean): MutableText =
    translate("useSkill.${if (active) "active" else "inactive"}", skill.name)