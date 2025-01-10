package com.imoonday.advskills_re.client.render

import com.imoonday.advskills_re.util.*
import kotlinx.serialization.*
import net.minecraft.text.*

@Serializable
enum class AnimationDirection {

    @SerialName("up")
    UP {

        override fun handleOffset(x: Int, y: Int, offset: Int): Pair<Int, Int> = x to y - offset
    },

    @SerialName("down")
    DOWN {

        override fun handleOffset(x: Int, y: Int, offset: Int): Pair<Int, Int> = x to y + offset
    },

    @SerialName("left")
    LEFT {

        override fun handleOffset(x: Int, y: Int, offset: Int): Pair<Int, Int> = x - offset to y
    },

    @SerialName("right")
    RIGHT {

        override fun handleOffset(x: Int, y: Int, offset: Int): Pair<Int, Int> = x + offset to y
    };

    val displayName: Text = translate("animationDirection.${name.lowercase()}")

    abstract fun handleOffset(x: Int, y: Int, offset: Int): Pair<Int, Int>
}