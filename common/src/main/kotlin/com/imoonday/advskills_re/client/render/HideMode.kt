package com.imoonday.advskills_re.client.render

import com.imoonday.advskills_re.util.*
import kotlinx.serialization.*
import net.minecraft.text.*

@Serializable
enum class HideMode {

    @SerialName("show")
    SHOW,

    @SerialName("dynamically_hide")
    DYNAMICALLY_HIDE,

    @SerialName("hide")
    HIDE;

    val displayName: Text = translate("hideMode.${name.lowercase()}")
}