package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.text.*
import net.minecraft.util.*

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
                        .suggests { context, builder1 ->
                            CommandSource.suggestMatching(context.skillConfig.skillBlackList.map {
                                it.replace(":", ".")
                            }, builder1)
                        }.executes(::removeSkill)
                )
        ).then(literal("list").executes(::queryBlackList))
    }

    private fun addSkill(context: CommandContext<ServerCommandSource>): Int {
        val skill = SkillArgumentType.getSkill(context)
        context.skillConfig.addBlackList(skill.id)
        context.syncConfig()
        context.sendFeedback("blacklist.add", skill.name)
        return 1
    }

    private fun removeSkill(context: CommandContext<ServerCommandSource>): Int {
        val idStr = StringArgumentType.getString(context, "id").replace(".", ":")
        val id = Identifier.tryParse(idStr)
        return if (id != null && context.skillConfig.removeBlackList(id)) {
            context.syncConfig()
            context.sendFeedback("blacklist.remove", Skills.fromIdNullable(id)?.name ?: idStr)
            1
        } else {
            context.sendFeedback("blacklist.not_found", Skills.fromIdNullable(id)?.name ?: idStr)
            0
        }
    }

    private fun queryBlackList(context: CommandContext<ServerCommandSource>): Int {
        val blackList = context.skillConfig.skillBlackList
        if (blackList.isEmpty()) {
            context.sendFeedback("blacklist.empty")
            return 0
        }
        var listText = Text.empty()
        val size = blackList.size
        blackList.forEachIndexed { index, id ->
            val skill = Skills.fromId(id)
            if (!skill.isEmpty()) {
                listText = listText.append(skill.getNameWithHoverEvent(context.source.world))
                if (index < size - 1) {
                    listText = listText.append(", ")
                }
            } else if (index == size - 1) {
                listText.siblings.removeLast()
            }
        }
        context.sendMessage(translate("blacklist.query", listText))
        return size
    }
}