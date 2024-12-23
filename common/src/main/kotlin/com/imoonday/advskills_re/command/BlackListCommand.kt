package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*

object BlackListCommand : BaseCommand("blacklist") {

    override fun build(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> {
        return builder.then(
            literal("add")
                .then(
                    literal("local")
                        .then(
                            argument("skill", SkillArgumentType.skill())
                                .executes { addSkill(it, false) }
                        )
                )
                .then(
                    literal("global")
                        .then(
                            argument("skill", SkillArgumentType.skill())
                                .executes { addSkill(it, true) }
                        )
                )
        )
            .then(
                literal("remove")
                    .then(
                        literal("local")
                            .then(
                                argument("id", StringArgumentType.string())
                                    .suggests { _, builder1 ->
                                        CommandSource.suggestMatching(SkillConfig.get().skillBlackList.map {
                                            it.replace(":", ".")
                                        }, builder1)
                                    }
                                    .executes { removeSkill(it, false) }
                            )
                    )
                    .then(
                        literal("global")
                            .then(
                                argument("id", StringArgumentType.string())
                                    .suggests { _, builder1 ->
                                        CommandSource.suggestMatching(GlobalConfig.get().skillConfig.skillBlackList.map {
                                            it.replace(":", ".")
                                        }, builder1)
                                    }
                                    .executes { removeSkill(it, true) }
                            )
                    )
            )
            .then(
                literal("list")
                    .then(
                        literal("local")
                            .executes { queryBlackList(it, false) }
                    )
                    .then(
                        literal("global")
                            .executes { queryBlackList(it, true) }
                    )
            )
            .then(
                literal("clear")
                    .then(
                        literal("local")
                            .executes { clearBlackList(it, false) }
                    )
                    .then(
                        literal("global")
                            .executes { clearBlackList(it, true) }
                    )
            )
    }

    private fun addSkill(context: CommandContext<ServerCommandSource>, global: Boolean): Int {
        val skill = SkillArgumentType.getSkill(context)
        getSkillConfig(global).addBlackList(skill.id)
        trySave(global)
        context.syncConfig(global)
        context.sendFeedback("blacklist.add", skill.name)
        return 1
    }

    private fun removeSkill(context: CommandContext<ServerCommandSource>, global: Boolean): Int {
        val idStr = StringArgumentType.getString(context, "id").replace(".", ":")
        val id = idStr.toIdentifier()
        return if (id != null && getSkillConfig(global).removeBlackList(id)) {
            trySave(global)
            context.syncConfig(global)
            context.sendFeedback("blacklist.remove", Skills.fromIdNullable(id)?.name ?: idStr)
            1
        } else {
            context.sendFeedback("blacklist.not_found", Skills.fromIdNullable(id)?.name ?: idStr)
            0
        }
    }

    private fun queryBlackList(context: CommandContext<ServerCommandSource>, global: Boolean): Int {
        val blackList = getSkillConfig(global).skillBlackList
        if (blackList.isEmpty()) {
            context.sendFeedback("blacklist.empty")
            return 0
        }
        val listText = blackList.toText(
            formatter = { id -> Skills.fromId(id).takeUnless { it.isEmpty() }?.hoverableName },
            prefix = "[".toText(),
            suffix = "]".toText()
        )
        context.sendMessage(translate("blacklist.query", listText))
        return blackList.size
    }

    private fun clearBlackList(context: CommandContext<ServerCommandSource>, global: Boolean): Int {
        getSkillConfig(global).skillBlackList.clear()
        trySave(global)
        context.syncConfig(global)
        context.sendFeedback("blacklist.clear")
        return 1
    }
}