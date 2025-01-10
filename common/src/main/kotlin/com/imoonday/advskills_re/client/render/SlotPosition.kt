package com.imoonday.advskills_re.client.render

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.util.*
import kotlinx.serialization.*
import net.minecraft.client.network.*
import net.minecraft.text.*
import net.minecraft.util.*
import kotlin.Pair

@Serializable
enum class SlotPosition {

    @SerialName("left_bottom")
    LEFT_BOTTOM {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> =
            ClientConfig.get().selectedSlotOffsetX to windowHeight - slotHeight + ClientConfig.get().selectedSlotOffsetY
    },

    @SerialName("left_of_hotbar")
    LEFT_OF_HOTBAR {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> =
            windowWidth / 2 - 91 - 29 - (if (player.mainArm == Arm.LEFT || player.offHandStack.isEmpty) 0 else slotWidth + 7) + ClientConfig.get().selectedSlotOffsetX to windowHeight - slotHeight + ClientConfig.get().selectedSlotOffsetY
    },

    @SerialName("right_of_hotbar")
    RIGHT_OF_HOTBAR {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> =
            windowWidth / 2 + 91 + (if (player.mainArm == Arm.RIGHT || player.offHandStack.isEmpty) 0 else slotWidth + 7) + ClientConfig.get().selectedSlotOffsetX to windowHeight - slotHeight + ClientConfig.get().selectedSlotOffsetY
    },

    @SerialName("right_bottom")
    RIGHT_BOTTOM {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> =
            windowWidth - slotWidth + ClientConfig.get().selectedSlotOffsetX to windowHeight - slotHeight + ClientConfig.get().selectedSlotOffsetY
    },

    @SerialName("center")
    CENTER {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> =
            (windowWidth - slotWidth) / 2 + ClientConfig.get().selectedSlotOffsetX to (windowHeight - slotHeight) / 2 + ClientConfig.get().selectedSlotOffsetY
    },

    @SerialName("left_center")
    LEFT_CENTER {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> =
            ClientConfig.get().selectedSlotOffsetX to (windowHeight - slotHeight) / 2 + ClientConfig.get().selectedSlotOffsetY
    },

    @SerialName("right_center")
    RIGHT_CENTER {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> =
            windowWidth - slotWidth + ClientConfig.get().selectedSlotOffsetX to (windowHeight - slotHeight) / 2 + ClientConfig.get().selectedSlotOffsetY
    },

    @SerialName("top_center")
    TOP_CENTER {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> =
            (windowWidth - slotWidth) / 2 + ClientConfig.get().selectedSlotOffsetX to ClientConfig.get().selectedSlotOffsetY
    },

    @SerialName("left_top")
    LEFT_TOP {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> = ClientConfig.get().selectedSlotOffsetX to ClientConfig.get().selectedSlotOffsetY
    },

    @SerialName("right_top")
    RIGHT_TOP {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> =
            windowWidth - slotWidth + ClientConfig.get().selectedSlotOffsetX to ClientConfig.get().selectedSlotOffsetY
    },

    @SerialName("custom")
    CUSTOM {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> = ClientConfig.get().selectedSlotOffsetX to ClientConfig.get().selectedSlotOffsetY
    },

    @SerialName("custom_percent")
    CUSTOM_PERCENT {

        override fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int> =
            ((windowWidth - slotWidth) * (ClientConfig.get().selectedSlotOffsetX / 100.0)).toInt() to ((windowHeight - slotHeight) * (ClientConfig.get().selectedSlotOffsetY / 100.0)).toInt()
    };

    val displayName: Text = translate("slotPosition.${name.lowercase()}")

    abstract fun getPosition(
        player: ClientPlayerEntity,
        windowWidth: Int,
        windowHeight: Int,
        slotWidth: Int,
        slotHeight: Int
    ): Pair<Int, Int>
}