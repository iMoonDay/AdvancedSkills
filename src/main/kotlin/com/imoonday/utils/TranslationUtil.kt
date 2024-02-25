package com.imoonday.utils

import net.minecraft.text.Text

object TranslationUtil {

    fun skillTranslate(name: String, field: String): Text = Text.translatable("advancedSkills.skill.$name.$field")

    fun skillName(name: String): Text = skillTranslate(name, "name")

    fun skillDescription(name: String): Text = skillTranslate(name, "description")
}