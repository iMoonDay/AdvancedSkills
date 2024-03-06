package com.imoonday.util

import com.imoonday.init.ModSkills
import com.imoonday.skill.Skill
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import java.util.concurrent.CompletableFuture

class SkillArgumentType : ArgumentType<Skill> {
    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> =
        CommandSource.suggestIdentifiers(ModSkills.SKILLS.filterNot { it.invalid }.map { it.id }, builder)

    override fun parse(reader: StringReader): Skill {
        val i = reader.cursor

        while (reader.canRead() && Identifier.isCharValid(reader.peek())) {
            reader.skip()
        }

        val string = reader.string.substring(i, reader.cursor)
        try {
            val id = Identifier(string)
            val skill = ModSkills.getOrNull(id) ?: throw UNKNOWN.create()
            if (skill.invalid) throw INVALID.create()
            return skill
        } catch (e: InvalidIdentifierException) {
            reader.cursor = i
            throw INVALID.create()
        }
    }

    companion object {
        val INVALID = SimpleCommandExceptionType(translate("command", "invalid"))
        val UNKNOWN = SimpleCommandExceptionType(translate("command", "unknown"))
        fun register() = ArgumentTypeRegistry.registerArgumentType(
            id("skill"),
            SkillArgumentType::class.java,
            ConstantArgumentSerializer.of(SkillArgumentType::skill)
        )

        fun skill(): SkillArgumentType = SkillArgumentType()

        fun getSkill(context: CommandContext<*>): Skill = context.getArgument("skill", Skill::class.java)
    }
}