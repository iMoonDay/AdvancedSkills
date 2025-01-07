package com.imoonday.advskills_re.init

import com.imoonday.advskills_re.init.ModItems.ITEMS
import com.imoonday.advskills_re.item.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import com.mojang.logging.*
import net.minecraft.server.*
import net.minecraft.util.*
import org.slf4j.*

object Skills {

    private val LOGGER: Logger = LogUtils.getLogger()
    private val factories = mutableMapOf<Identifier, () -> Skill>()
    private val skills = linkedMapOf<Identifier, Skill>()

    @JvmField
    val EMPTY = register { EmptySkill }

    @JvmField
    val FIREBALL = register(::FireballSkill)

    @JvmField
    val HORIZONTAL_DASH = register(::HorizontalDashSkill)

    @JvmField
    val TELEPORT = register(::TeleportSkill)

    @JvmField
    val JUMP = register(::JumpSkill)

    @JvmField
    val SUPER_JUMP = register(::DoubleJumpSkill)

    @JvmField
    val TRIPLE_JUMP = register(::TripleJumpSkill)

    @JvmField
    val DASH = register(::DashSkill)

    @JvmField
    val GROUND_WHACK = register(::GroundWhackSkill)

    @JvmField
    val ABSOLUTE_DEFENSE = register(::AbsoluteDefenseSkill)

    @JvmField
    val EXTREME_BOUNCE = register(::ExtremeBounceSkill)

    @JvmField
    val RAPID_BOUNCE = register(::RapidBounceSkill)

    @JvmField
    val PERFECT_BOUNCE = register(::PerfectBounceSkill)

    @JvmField
    val MICRO_BOUNCE = register(::MicroBounceSkill)

    @JvmField
    val WALL_CLIMBING = register(::WallClimbingSkill)

    @JvmField
    val PIERCING = register(::PiercingSkill)

    @JvmField
    val PRIMARY_HEALING = register(::PrimaryHealingSkill)

    @JvmField
    val INSTANT_HEALING = register(::InstantHealingSkill)

    @JvmField
    val INTERMEDIATE_HEALING = register(::IntermediateHealingSkill)

    @JvmField
    val ADVANCED_HEALING = register(::AdvancedHealingSkill)

    @JvmField
    val TOP_HEALING = register(::TopHealingSkill)

    @JvmField
    val EXTREME_EVASION = register(::ExtremeEvasionSkill)

    @JvmField
    val SELF_HEALING = register(::SelfHealingSkill)

    @JvmField
    val STRONG_PHYSIQUE = register(::StrongPhysiqueSkill)

    @JvmField
    val AGILITY = register(::AgilitySkill)

    @JvmField
    val RESURRECTION = register(::ResuscitationSkill)

    @JvmField
    val DYING_COUNTERATTACK = register(::DyingCounterattackSkill)

    @JvmField
    val DISARM = register(::DisarmSkill)

    @JvmField
    val PRIMARY_SILENCE = register(::PrimarySilenceSkill)

    @JvmField
    val LAST_DITCH_EFFORT = register(::LastDitchEffortSkill)

    @JvmField
    val MASTERY = register(::MasterySkill)

    @JvmField
    val PRIMARY_PURIFICATION = register(::PrimaryPurificationSkill)

    @JvmField
    val ADVANCED_PURIFICATION = register(::AdvancedPurificationSkill)

    @JvmField
    val ABSOLUTE_DOMAIN = register(::AbsoluteDomainSkill)

    @JvmField
    val CHARGED_SWEEP = register(::ChargedSweepSkill)

    @JvmField
    val ACTIVE_DEFENSE = register(::ActiveDefenseSkill)

    @JvmField
    val SELF_REPAIR = register(::SelfRepairSkill)

    @JvmField
    val INSTANT_EXPLOSIVE = register(::InstantExplosiveSkill)

    @JvmField
    val PRIMARY_FREEZE = register(::PrimaryFreezeSkill)

    @JvmField
    val PRIMARY_SLOWNESS = register(::PrimarySlownessSkill)

    @JvmField
    val PRIMARY_CONFINEMENT = register(::PrimaryConfinementSkill)

    @JvmField
    val EXCLUSIVE_MOUNT = register(::ExclusiveMountSkill)

    @JvmField
    val NIGHT_VISION = register(::NightVisionSkill)

    @JvmField
    val UNDEAD_SUMMONING = register(::UndeadSummoningSkill)

    @JvmField
    val TAUNT = register(::TauntSkill)

    @JvmField
    val LIQUID_SHIELD = register(::LiquidShieldSkill)

    @JvmField
    val WATER_WALKER = register(::WaterWalkerSkill)

    @JvmField
    val AUTOMATIC_UPHILL = register(::AutomaticUphillSkill)

    @JvmField
    val WATER_BREATHING = register(::WaterBreathingSkill)

    @JvmField
    val STATIC_INVISIBILITY = register(::StaticInvisibilitySkill)

    @JvmField
    val FASTER_EATING = register(::FasterEatingSkill)

    @JvmField
    val DANGER_PERCEPTION = register(::DangerPerceptionSkill)

    @JvmField
    val RISING_SHOCK = register(::RisingShockSkill)

    @JvmField
    val CATAPULT_GLIDING = register(::CatapultGlidingSkill)

    @JvmField
    val CHARGED_DASH = register(::ChargedDashSkill)

    @JvmField
    val LASER_EYE = register(::LaserEyeSkill)

    @JvmField
    val METEOR_SHOWER = register(::MeteorShowerSkill)

    @JvmField
    val NEGATIVE_RESISTANCE = register(::NegativeResistanceSkill)

    @JvmField
    val INSIGHTFUL_EYE = register(::InsightfulEyeSkill)

    @JvmField
    val ITEM_ATTRACTION = register(::ItemAttractionSkill)

    @JvmField
    val DOPING = register(::DopingSkill)

    @JvmField
    val GRAPPLING_HOOK = register(::GrapplingHookSkill)

    @JvmField
    val REVERSE_GRAVITY = register(::ReverseGravitySkill)

    @JvmField
    val ORE_PERCEPTION = register(::OrePerceptionSkill)

    @JvmField
    val INVISIBLE_TRAP = register(::InvisibleTrapSkill)

    @JvmField
    val FROST_TRAP = register(::FrostTrapSkill)

    @JvmField
    val TEMPORARY_SHIELD = register(::TemporaryShieldSkill)

    @JvmField
    val PAIN_FEEDBACK = register(::PainFeedbackSkill)

    @JvmField
    val THUNDER_FURY = register(::ThunderFurySkill)

    @JvmField
    val COUNTERBLAST = register(::CounterblastSkill)

    @JvmField
    val TIME_REWIND = register(::TimeRewindSkill)

    @JvmField
    val LIVING_DETECTION = register(::LivingDetectionSkill)

    @JvmField
    val BLOOD_SEAL = register(::BloodSealSkill)

    @JvmField
    val SWORD_SOUL_GUARDING = register(::SwordSoulGuardingSkill)

    @JvmField
    val WIND_BLADE = register(::WindBladeSkill)

    @JvmField
    val BIOLOGICAL_HOOK = register(::BiologicalHookSkill)

    @JvmField
    val DUPLICATION = register(::DuplicationSkill)

    @JvmField
    val MAGNETIC_TRAP = register(::MagneticTrapSkill)

    @JvmField
    val MULTIPLE_LASER = register(::MultipleLaserSkill)

    @JvmField
    val SUPER_SHADOW_CLONE = register(::SuperShadowCloneSkill)

    @JvmField
    val ARROW_RAIN = register(::ArrowRainSkill)

    @JvmField
    val WEED_CLEANER = register(::WeedCleanerSkill)

    @JvmField
    val UNHINDERED_STRIDE = register(::UnhinderedStrideSkill)

    @JvmField
    val DAMAGE_ABSORPTION = register(::DamageAbsorptionSkill)

    @JvmField
    val DISGUISE = register(::DisguiseSkill)

    @JvmField
    val WALL_JUMP = register(::WallJumpSkill)

    @JvmField
    val RETURN = register(::ReturnSkill)

    @JvmField
    val PORTABLE_CHEST = register(::PortableChestSkill)

    @JvmField
    val LAVA_WALKER = register(::LavaWalkerSkill)

    @JvmField
    val ARMOR_SHATTERER = register(::ArmorShattererSkill)

    @JvmField
    val DEATH_ARCHIVE = register(::DeathArchiveSkill)

    @JvmField
    val WITHERING_TOUCH = register(::WitheringTouchSkill)

    @JvmField
    val FORCED_GROWTH = register(::ForcedGrowthSkill)

    @JvmField
    val SPACE_BLAST = register(::SpaceBlastSkill)

    @JvmField
    val GLIDING = register(::GlidingSkill)

    @JvmField
    val LIGHT_LANDING = register(::LightLandingSkill)

    @JvmField
    val FOODLESS = register(::FoodlessSkill)

    @JvmStatic
    fun init() = Unit

    @JvmStatic
    fun reload(server: MinecraftServer) {
        val skills = skills.values.filterNot { it.isEmpty }
        SettingsManager.loadOrSaveFiles(skills)
        val serverSkills = SettingsManager.loadFromServerConfig(server)
        skills.forEach {
            (serverSkills[it.id]
                ?: SettingsManager.getSettings(it)
                ?: createDefaultSkill(it.id)?.settings)
                ?.run { it.updateSettings(this) }
        }
    }

    @JvmStatic
    fun updateSettings(settings: Collection<Skill.Settings>) {
        settings.forEach { skills[it.id]?.updateSettings(it) }
    }

    @JvmStatic
    fun <T : Skill> register(factory: () -> T): T {
        val skill = factory()
        if (skill.id in skills.keys || skill in skills.values) {
            LOGGER.warn("Skill ${skill.id} is already registered")
            return skill
        }
        if (!skill.isEmpty) ITEMS.register(skill.id.path) { SkillItem(skill) }
        factories[skill.id] = factory
        skills[skill.id] = skill
        return skill
    }

    @JvmStatic
    fun compareIndex(skill1: Skill, skill2: Skill): Int {
        val skillValues = skills.values
        return skillValues.indexOf(skill1).compareTo(skillValues.indexOf(skill2))
    }

    @JvmStatic
    fun getSkills(): List<Skill> = skills.values.filterNot { it.isEmpty }

    @JvmStatic
    fun getEnabledSkills(): List<Skill> = skills.values.filterNot { it.disabled }

    @JvmStatic
    fun fromId(id: Identifier): Skill = skills.getOrDefault(id, EMPTY)

    @JvmStatic
    fun fromId(id: String): Skill =
        if (id.contains(":")) id.toIdentifier()?.let { fromId(it) } ?: EMPTY else fromId(id(id))

    @JvmStatic
    fun fromIdNullable(id: Identifier?): Skill? = skills[id]

    @JvmStatic
    fun fromIdNullable(id: String?): Skill? =
        fromIdNullable(id?.let { if (it.contains(":")) it.toIdentifier() else id(it) })

    @JvmStatic
    inline fun <reified T : SkillTrigger> getTriggers(predicate: (T) -> Boolean = { true }): List<T> =
        getSkills().filterIsInstance<T>().filter(predicate)

    @JvmStatic
    fun getLearnableSkills(
        except: Collection<Skill> = emptyList(),
        filter: (Skill) -> Boolean = { true },
    ): List<Skill> = getEnabledSkills()
        .filterNot { it in except }
        .filter(filter)

    @JvmStatic
    fun random(
        except: Collection<Skill> = emptyList(),
        filter: (Skill) -> Boolean = { true }
    ): Skill = getLearnableSkills(except, filter).randomByWeight(Skill::weight, EMPTY)

    @JvmStatic
    fun createDefaultSkill(id: Identifier): Skill? = factories[id]?.invoke()
}