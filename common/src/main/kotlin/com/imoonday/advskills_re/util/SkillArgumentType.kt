package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.skill.*
import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.context.*
import com.mojang.brigadier.exceptions.*
import com.mojang.brigadier.suggestion.*
import net.minecraft.command.*
import net.minecraft.util.*
import java.util.concurrent.*

class SkillArgumentType : ArgumentType<Skill> {

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> =
        CommandSource.suggestFromIdentifier(Skill.getValidSkills(), builder, Skill::id, Skill::name)

    override fun parse(reader: StringReader): Skill {
        val i = reader.cursor

        while (reader.canRead() && Identifier.isCharValid(reader.peek())) {
            reader.skip()
        }
        val string = reader.string.substring(i, reader.cursor)
        try {
            val id = if (":" in string) Identifier(string) else id(string)
            val skill = Skill.fromIdNullable(id) ?: throw UNKNOWN.create()
            if (skill.invalid) throw INVALID.create()
            return skill
        } catch (e: InvalidIdentifierException) {
            reader.cursor = i
            throw INVALID.create()
        }
    }

    companion object {

        val INVALID = SimpleCommandExceptionType(translate("command.invalid"))
        val UNKNOWN = SimpleCommandExceptionType(translate("command.unknown"))

        fun skill(): SkillArgumentType = SkillArgumentType()

        fun getSkill(context: CommandContext<*>): Skill = context.getArgument("skill", Skill::class.java)
    }
}