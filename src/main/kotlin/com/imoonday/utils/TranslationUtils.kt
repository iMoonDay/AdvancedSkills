package com.imoonday.utils

import net.minecraft.text.MutableText
import net.minecraft.text.Text

/**
 *  @return advancedSkills.name.key
 */
fun translate(name: String, key: String?, vararg args: Any): MutableText =
    Text.translatable("advancedSkills.$name${key?.let { ".$it" } ?: ""}", *args)

fun translateSkill(name: String, key: String, vararg args: Any): MutableText = translate("skill", "$name.$key", *args)