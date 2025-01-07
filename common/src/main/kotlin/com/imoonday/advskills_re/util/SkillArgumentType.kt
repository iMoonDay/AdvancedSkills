package com.imoonday.advskills_re.util

import com.google.gson.*
import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.init.*
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
import java.util.function.*
import java.util.function.Function

class SkillArgumentType(private val containsDisabled: Boolean) : ArgumentType<Skill> {

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val skills = if (containsDisabled) Skills.getSkills() else Skills.getEnabledSkills()
        val string = builder.remaining.lowercase()
        forEachMatching(skills, string, Skill::id) {
            builder.suggest(it.id.toString(), it.name)
            if (it.id.namespace == MOD_ID) {
                builder.suggest(it.id.path, it.name)
            }
        }
        return builder.buildFuture()
    }

    override fun parse(reader: StringReader): Skill {
        val i = reader.cursor

        while (reader.canRead() && Identifier.isCharValid(reader.peek())) {
            reader.skip()
        }
        val string = reader.string.substring(i, reader.cursor)
        try {
            val skill = Skills.fromIdNullable(string) ?: throw UNKNOWN.create()
            if (!containsDisabled && skill.disabled || skill.isEmpty) throw INVALID.create()
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
            Properties(argumentType.containsDisabled)

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

        @JvmStatic
        fun <T> forEachMatching(
            candidates: Iterable<T>,
            remaining: String,
            identifier: Function<T, Identifier>,
            action: Consumer<T>
        ) {
            val bl = remaining.indexOf(58.toChar()) > -1

            for (t in candidates) {
                val identifier2 = identifier.apply(t)
                if (bl) {
                    val string = identifier2.toString()
                    if (CommandSource.shouldSuggest(remaining, string)) {
                        action.accept(t)
                    }
                } else if (CommandSource.shouldSuggest(remaining, identifier2.namespace)
                    || identifier2.namespace == MOD_ID && CommandSource.shouldSuggest(remaining, identifier2.path)
                ) {
                    action.accept(t)
                }
            }
        }
    }
}