package com.imoonday.custom

import com.imoonday.skill.Skill
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.id
import kotlinx.serialization.json.*
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class CustomSkill(
    id: Identifier,
    val nameKey: String,
    val descriptionKey: String? = null,
    val iconId: Identifier? = null,
    types: List<SkillType>? = null,
    val cooldown: Int? = null,
    rarity: Rarity? = null,
    val soundId: Identifier? = null,
    val event: Event,
) : Skill(
    id,
    Text.translatable(nameKey),
    descriptionKey?.let { Text.translatable(descriptionKey) } ?: Text.empty(),
    iconId ?: id("unknown.png"),
    types ?: emptyList(),
    cooldown ?: 0,
    rarity ?: Rarity.USELESS,
    soundId?.let { Registries.SOUND_EVENT.get(it) },
) {

    override fun use(user: ServerPlayerEntity): UseResult = event.run(user)

    fun toJson(): JsonObject = buildJsonObject {
        put("id", id.toString())
        put("name", nameKey)
        descriptionKey?.let { put("description", it) }
        iconId?.let { put("icon", it.toString()) }
        putJsonArray("types") {
            types.map { it.name }.forEach { add(it) }
        }
        put("cooldown", defaultCooldown)
        put("rarity", rarity.name)
        soundId?.let { put("sound", it.toString()) }
        put("event", CustomSkillHandler.JSON.encodeToJsonElement(event))
    }

    companion object {

        fun fromJson(json: JsonObject): CustomSkill? {
            val id = json["id"]?.jsonPrimitive?.contentOrNull?.let { Identifier.tryParse(it) } ?: return null
            val nameKey = json["name"]?.jsonPrimitive?.contentOrNull ?: return null
            val descriptionKey = json["description"]?.jsonPrimitive?.contentOrNull
            val icon = json["icon"]?.jsonPrimitive?.contentOrNull?.let { Identifier.tryParse(it) }
            val types = json["types"]?.jsonArray?.mapNotNull {
                it.jsonPrimitive.contentOrNull?.let { name ->
                    SkillType.valueOf(name)
                }
            }
            val defaultCooldown = json["cooldown"]?.jsonPrimitive?.intOrNull
            val rarity = json["rarity"]?.jsonPrimitive?.contentOrNull?.let { Rarity.valueOf(it) }
            val soundId = json["sound"]?.jsonPrimitive?.contentOrNull?.let { Identifier.tryParse(it) }
            val event = json["event"]?.let { Json.decodeFromJsonElement<Event>(it) } ?: return null

            return CustomSkill(
                id,
                nameKey,
                descriptionKey,
                icon,
                types,
                defaultCooldown,
                rarity,
                soundId,
                event
            )
        }
    }
}