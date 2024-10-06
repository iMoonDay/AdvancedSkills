package com.imoonday.util

import net.minecraft.text.*

fun translate(name: String, key: String? = null, vararg args: Any): MutableText =
    Text.translatable("advancedSkills.$name${key?.let { ".$it" } ?: ""}", *args)

fun translateSkill(name: String, key: String, vararg args: Any): MutableText = translate("skill", "$name.$key", *args)

fun translateActive(active: Boolean, name: String): MutableText =
    translate("useSkill", if (active) "active" else "inactive", name)