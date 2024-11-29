package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.*
import net.minecraft.text.*

fun translate(key: String, vararg args: Any): MutableText =
    Text.translatable("$MOD_ID.$key", *args)

fun translateSkill(id: String, key: String, vararg args: Any): MutableText = translate("skill.$id.$key", *args)

fun translateActive(active: Boolean, name: Text): MutableText =
    translate("useSkill.${if (active) "active" else "inactive"}", name)