package com.imoonday.advskills_re.init

import com.imoonday.advskills_re.command.*
import com.imoonday.advskills_re.command.Command.Companion.register
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
                CooldownCommand,
                XpMultiplierCommand,
                BlackListCommand,
                DefaultSlotsCommand,
                EnhanceCommand,
                DeEnhanceCommand,
            )
        }
    }
}