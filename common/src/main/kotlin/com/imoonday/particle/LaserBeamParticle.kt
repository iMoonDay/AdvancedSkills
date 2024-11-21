package com.imoonday.particle

import com.imoonday.init.*
import com.imoonday.skill.*
import com.imoonday.util.*
import com.mojang.brigadier.*
import com.mojang.serialization.*
import com.mojang.serialization.codecs.*
import net.minecraft.client.particle.*
import net.minecraft.client.render.*
import net.minecraft.client.world.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.network.*
import net.minecraft.particle.*
import net.minecraft.registry.*
import net.minecraft.util.math.*

class LaserBeamParticle(
    world: ClientWorld,
    x: Double,
    y: Double,
    z: Double,
    private val effect: Effect,
    spriteProvider: SpriteProvider
) : SpriteBillboardParticle(world, x, y, z) {

    private val source: Entity? = world.getEntityById(effect.sourceId)

    init {
        this.maxAge = effect.duration
        setColor(
            ColorHelper.Argb.getRed(effect.color) / 255.0f,
            ColorHelper.Argb.getGreen(effect.color) / 255.0f,
            ColorHelper.Argb.getBlue(effect.color) / 255.0f
        )
        this.alpha = ColorHelper.Argb.getAlpha(effect.color) / 255.0f
        this.scale = 0.25f
        setSpriteForAge(spriteProvider)
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE // 渲染类型

    override fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        if (source is PlayerEntity && source.isUsing(Skills.MULTIPLE_LASER)) {
            super.buildGeometry(vertexConsumer, camera, tickDelta)
        } else {
            markDead()
        }
    }

    override fun tick() {
        super.tick()
        val start = source?.eyePos ?: return
        val end = (source as PlayerEntity).raycastVisualBlock(256.0).pos
        val lerp = effect.lerp
        val pos = start.lerp(end, lerp.toDouble())
        setPos(pos.x, pos.y, pos.z)
    }

    class Effect(
        var sourceId: Int,
        val end: Vec3d,
        var lerp: Float,
        var duration: Int,
        val color: Int
    ) : ParticleEffect {

        override fun getType(): Type = ModParticleTypes.LASER_BEAM.get()

        override fun write(buf: PacketByteBuf) {
            buf.writeInt(sourceId)
            buf.writeDouble(end.x)
            buf.writeDouble(end.y)
            buf.writeDouble(end.z)
            buf.writeFloat(lerp)
            buf.writeInt(duration)
            buf.writeInt(color)
        }

        override fun asString(): String =
            "${
                Registries.PARTICLE_TYPE.getId(type)
                    .toString()
            } { sourceId: $sourceId, end: $end, lerp: $lerp, duration: $duration, color: $color }"
    }

    class Factory(private val spriteProvider: SpriteProvider) : ParticleFactory<Effect> {

        override fun createParticle(
            parameters: Effect,
            world: ClientWorld,
            x: Double,
            y: Double,
            z: Double,
            velocityX: Double,
            velocityY: Double,
            velocityZ: Double
        ): Particle = LaserBeamParticle(world, x, y, z, parameters, spriteProvider)
    }

    class Type : ParticleType<Effect>(true, FACTORY) {

        private val codec: Codec<Effect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("source_id").forGetter { it.sourceId },
                Codec.DOUBLE.fieldOf("end_x").forGetter { it.end.x },
                Codec.DOUBLE.fieldOf("end_y").forGetter { it.end.y },
                Codec.DOUBLE.fieldOf("end_z").forGetter { it.end.z },
                Codec.FLOAT.fieldOf("lerp").forGetter { it.lerp },
                Codec.INT.fieldOf("duration").forGetter { it.duration },
                Codec.INT.fieldOf("color").forGetter { it.color }
            ).apply(instance) { sourceId, endX, endY, endZ, lerp, duration, color ->
                Effect(
                    sourceId,
                    Vec3d(endX, endY, endZ),
                    lerp,
                    duration,
                    color
                )
            }
        }

        override fun getCodec(): Codec<Effect> = codec

        companion object {

            private val FACTORY = object : ParticleEffect.Factory<Effect> {
                override fun read(type: ParticleType<Effect>, reader: StringReader): Effect {
                    val sourceId = reader.readInt()
                    val end = Vec3d(reader.readDouble(), reader.readDouble(), reader.readDouble())
                    val lerp = reader.readFloat()
                    val duration = reader.readInt()
                    val color = reader.readInt()
                    return Effect(sourceId, end, lerp, duration, color)
                }

                override fun read(type: ParticleType<Effect>, buf: PacketByteBuf): Effect {
                    val sourceId = buf.readInt()
                    val end = Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble())
                    val lerp = buf.readFloat()
                    val duration = buf.readInt()
                    val color = buf.readInt()
                    return Effect(sourceId, end, lerp, duration, color)
                }
            }
        }
    }
}