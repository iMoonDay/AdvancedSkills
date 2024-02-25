package com.imoonday.utils

import com.imoonday.AdvancedSkills
import com.imoonday.skills.Skills
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import java.util.concurrent.CompletableFuture

class SkillArgumentType : ArgumentType<Skill> {
    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> = CommandSource.suggestIdentifiers(Skills.SKILLS.map { it.id }, builder)

    override fun parse(reader: StringReader): Skill {
        val i = reader.cursor

        while (reader.canRead() && Identifier.isCharValid(reader.peek())) {
            reader.skip()
        }

        val string = reader.string.substring(i, reader.cursor)
        try {
            val id = Identifier(string)
            val skill = Skills.get(id)
            if (skill.isEmpty && !id.path.equals("empty")) {
                throw UNKNOWN.create()
            }
            return skill
        } catch (e: InvalidIdentifierException) {
            reader.cursor = i
            throw UNKNOWN.create()
        }
    }

    companion object {
        val UNKNOWN = SimpleCommandExceptionType(Text.translatable("advancedSkills.command.invalid"))
        fun register() = ArgumentTypeRegistry.registerArgumentType(
            AdvancedSkills.id("skill"),
            SkillArgumentType::class.java,
            ConstantArgumentSerializer.of(SkillArgumentType::skill)
        )

        fun skill(): SkillArgumentType = SkillArgumentType()

        fun getSkill(context: CommandContext<*>): Skill = context.getArgument("skill", Skill::class.java)
    }
}