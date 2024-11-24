package com.imoonday.init

import com.imoonday.command.*
import com.imoonday.command.Command.Companion.register
import dev.architectury.event.events.common.*

object ModCommands {

    fun init() {
        CommandRegistrationEvent.EVENT.register { dispatcher, registry, selection ->
            dispatcher.register(
                registry, selection,
                LearnCommand,
                LearnAllCommand,
                ForgetCommand,
                ForgetAllCommand,
                EquipCommand,
                UnequipCommand,
                ResetCooldownCommand,
                ListCommand,
                SlotCommand,
                ResetDataCommand,
                ModifySkillCommand,
                AddXpCommand,
                SetXpCommand,
                QueryXpCommand,
                ResetXpCommand,
                CooldownCommand
            )
        }
    }
}