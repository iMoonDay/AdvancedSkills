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
                    argument("skill", SkillArgumentType.skill())
                        .executes(::addSkill)
                )
        ).then(
            literal("remove")
                .then(
                    argument("id", StringArgumentType.string())
                        .suggests { _, builder1 ->
                            CommandSource.suggestMatching(SkillConfig.get().skillBlackList.map {
                                it.replace(":", ".")
                            }, builder1)
                        }.executes(::removeSkill)
                )
        ).then(literal("list").executes(::queryBlackList))
            .then(literal("clear").executes(::clearBlackList))
    }

    private fun addSkill(context: CommandContext<ServerCommandSource>): Int {
        val skill = SkillArgumentType.getSkill(context)
        SkillConfig.get().addBlackList(skill.id)
        context.syncConfig()
        context.sendFeedback("blacklist.add", skill.name)
        return 1
    }

    private fun removeSkill(context: CommandContext<ServerCommandSource>): Int {
        val idStr = StringArgumentType.getString(context, "id").replace(".", ":")
        val id = idStr.toIdentifier()
        return if (id != null && SkillConfig.get().removeBlackList(id)) {
            context.syncConfig()
            context.sendFeedback("blacklist.remove", Skills.fromIdNullable(id)?.name ?: idStr)
            1
        } else {
            context.sendFeedback("blacklist.not_found", Skills.fromIdNullable(id)?.name ?: idStr)
            0
        }
    }

    private fun queryBlackList(context: CommandContext<ServerCommandSource>): Int {
        val blackList = SkillConfig.get().skillBlackList
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

    private fun clearBlackList(context: CommandContext<ServerCommandSource>): Int {
        SkillConfig.get().skillBlackList.clear()
        context.syncConfig()
        context.sendFeedback("blacklist.clear")
        return 1
    }
}