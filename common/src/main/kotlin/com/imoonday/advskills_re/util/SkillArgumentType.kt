package com.imoonday.advskills_re.util

import com.google.gson.*
import com.imoonday.advskills_re.skill.*
import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.context.*
import com.mojang.brigadier.exceptions.*
import com.mojang.brigadier.suggestion.*
import net.minecraft.command.*
import net.minecraft.command.argument.serialize.*
import net.minecraft.command.argument.serialize.ArgumentSerializer.*
import net.minecraft.network.*
import net.minecraft.util.*
import java.util.concurrent.*

class SkillArgumentType(private val containsInvalid: Boolean) : ArgumentType<Skill> {

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> =
        CommandSource.suggestFromIdentifier(
            if (containsInvalid) Skills.getSkillsNotEmpty()
            else Skills.getValidSkills(),
            builder,
            Skill::id,
            Skill::name
        )

    override fun parse(reader: StringReader): Skill {
        val i = reader.cursor

        while (reader.canRead() && Identifier.isCharValid(reader.peek())) {
            reader.skip()
        }
        val string = reader.string.substring(i, reader.cursor)
        try {
            val id = if (":" in string) Identifier(string) else id(string)
            val skill = Skills.fromIdNullable(id) ?: throw UNKNOWN.create()
            if (!containsInvalid && skill.isInvalid() || skill.isEmpty()) throw INVALID.create()
            return skill
        } catch (e: InvalidIdentifierException) {
            reader.cursor = i
            throw INVALID.create()
        }
    }

    class Serializer : ArgumentSerializer<SkillArgumentType, Serializer.Properties> {

        override fun writePacket(properties: Properties, buf: PacketByteBuf) {
            buf.writeBoolean(properties.containsInvalid)
        }

        override fun fromPacket(buf: PacketByteBuf): Properties = Properties(buf.readBoolean())

        override fun getArgumentTypeProperties(argumentType: SkillArgumentType): Properties =
            Properties(argumentType.containsInvalid)

        override fun writeJson(properties: Properties, json: JsonObject) =
            json.addProperty("containsInvalid", properties.containsInvalid)

        inner class Properties(
            val containsInvalid: Boolean
        ) : ArgumentTypeProperties<SkillArgumentType> {

            override fun createType(commandRegistryAccess: CommandRegistryAccess): SkillArgumentType =
                if (containsInvalid) skill() else validSkill()

            override fun getSerializer(): ArgumentSerializer<SkillArgumentType, *> = this@Serializer
        }
    }

    companion object {

        val INVALID = SimpleCommandExceptionType(translate("command.invalid"))
        val UNKNOWN = SimpleCommandExceptionType(translate("command.unknown"))

        fun validSkill(): SkillArgumentType = SkillArgumentType(false)

        fun skill(): SkillArgumentType = SkillArgumentType(true)

        fun getSkill(context: CommandContext<*>): Skill = context.getArgument("skill", Skill::class.java)
    }
}