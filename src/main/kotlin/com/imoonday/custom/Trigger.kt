package com.imoonday.custom

import kotlinx.serialization.Serializable

@Serializable
sealed interface Trigger : Task {

    override val type: String
        get() = "trigger"
    val trigger: String
}