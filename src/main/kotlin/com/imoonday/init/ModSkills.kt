package com.imoonday.init

import com.imoonday.skill.*
import com.imoonday.trigger.InitTrigger

object ModSkills {

    @JvmField
    val EMPTY = EmptySkill().register()

    @JvmField
    val FIREBALL = FireballSkill().register()

    @JvmField
    val HORIZONTAL_DASH = HorizontalDashSkill().register()

    @JvmField
    val TELEPORT = TeleportSkill().register()

    @JvmField
    val JUMP = JumpSkill().register()

    @JvmField
    val SUPER_JUMP = DoubleJumpSkill().register()

    @JvmField
    val TRIPLE_JUMP = TripleJumpSkill().register()

    @JvmField
    val DASH = DashSkill().register()

    @JvmField
    val GROUND_WHACK = GroundWhackSkill().register()

    @JvmField
    val ABSOLUTE_DEFENSE = AbsoluteDefenseSkill().register()

    @JvmField
    val EXTREME_REFLECTION = ExtremeReflectionSkill().register()

    @JvmField
    val RAPID_REFLECTION = RapidReflectionSkill().register()

    @JvmField
    val PERFECT_REFLECTION = PerfectReflectionSkill().register()

    @JvmField
    val MICRO_REFLECTION = MicroReflectionSkill().register()

    @JvmField
    val WALL_CLIMBING = WallClimbingSkill().register()

    @JvmField
    val PIERCING = PiercingSkill().register()

    @JvmField
    val PRIMARY_HEALING = PrimaryHealingSkill().register()

    @JvmField
    val INTERMEDIATE_HEALING = IntermediateHealingSkill().register()

    @JvmField
    val ADVANCED_HEALING = AdvancedHealingSkill().register()

    @JvmField
    val TOP_HEALING = TopHealingSkill().register()

    @JvmField
    val EXTREME_EVASION = ExtremeEvasionSkill().register()

    @JvmField
    val SELF_HEALING = SelfHealingSkill().register()

    @JvmField
    val STRONG_PHYSIQUE = StrongPhysiqueSkill().register()

    @JvmField
    val AGILITY = AgilitySkill().register()

    @JvmField
    val RESURRECTION = ResuscitationSkill().register()

    @JvmField
    val DYING_COUNTERATTACK = DyingCounterattackSkill().register()

    @JvmField
    val DISARM = DisarmSkill().register()

    @JvmField
    val PRIMARY_SILENCE = PrimarySilenceSkill().register()

    @JvmField
    val LAST_DITCH_EFFORT = LastDitchEffortSkill().register()

    @JvmField
    val MASTERY = MasterySkill().register()

    @JvmField
    val PRIMARY_PURIFICATION = PrimaryPurificationSkill().register()

    @JvmField
    val ADVANCED_PURIFICATION = AdvancedPurificationSkill().register()

    @JvmField
    val ABSOLUTE_DOMAIN = AbsoluteDomainSkill().register()

    @JvmField
    val CHARGED_SWEEP = ChargedSweepSkill().register()

    @JvmField
    val ACTIVE_DEFENSE = ActiveDefenseSkill().register()

    @JvmField
    val SELF_REPAIR = SelfRepairSkill().register()

    @JvmField
    val INSTANT_EXPLOSIVE = InstantExplosiveSkill().register()

    @JvmField
    val PRIMARY_FREEZE = PrimaryFreezeSkill().register()

    @JvmField
    val PRIMARY_SLOWNESS = PrimarySlownessSkill().register()

    @JvmField
    val PRIMARY_CONFINEMENT = PrimaryConfinementSkill().register()

    @JvmField
    val EXCLUSIVE_MOUNT = ExclusiveMountSkill().register()

    @JvmField
    val NIGHT_VISION = NightVisionSkill().register()

    @JvmField
    val UNDEAD_SUMMONING = UndeadSummoningSkill().register()

    @JvmField
    val TAUNT = TauntSkill().register()

    @JvmField
    val LIQUID_SHIELD = LiquidShieldSkill().register()

    @JvmField
    val WATER_WALKER = WaterWalkerSkill().register()

    @JvmField
    val AUTOMATIC_UPHILL = AutomaticUphillSkill().register()

    @JvmField
    val WATER_BREATHING = WaterBreathingSkill().register()

    @JvmField
    val STATIC_INVISIBILITY = StaticInvisibilitySkill().register()

    @JvmField
    val FASTER_EATING = FasterEatingSkill().register()

    @JvmField
    val DANGER_PERCEPTION = DangerPerceptionSkill().register()

    @JvmField
    val RISING_SHOCK = RisingShockSkill().register()

    @JvmField
    val CATAPULT_GLIDING = CatapultGlidingSkill().register()

    @JvmField
    val CHARGED_DASH = ChargedDashSkill().register()

    @JvmField
    val LASER_EYE = LaserEyeSkill().register()

    @JvmField
    val METEOR_SHOWER = MeteorShowerSkill().register()

    @JvmField
    val NEGATIVE_RESISTANCE = NegativeResistanceSkill().register()

    @JvmField
    val INSIGHTFUL_EYE = InsightfulEyeSkill().register()

    @JvmField
    val ITEM_ATTRACTION = ItemAttractionSkill().register()

    @JvmField
    val DOPING = DopingSkill().register()

    @JvmField
    val GRAPPLING_HOOK = GrapplingHookSkill().register()

    @JvmField
    val REVERSE_GRAVITY = ReverseGravitySkill().register()

    @JvmField
    val ORE_PERCEPTION = OrePerceptionSkill().register()

    @JvmField
    val INVISIBLE_TRAP = InvisibleTrapSkill().register()

    @JvmField
    val FROST_TRAP = FrostTrapSkill().register()

    @JvmField
    val TEMPORARY_SHIELD = TemporaryShieldSkill().register()

    @JvmField
    val PAIN_FEEDBACK = PainFeedbackSkill().register()

    @JvmField
    val THUNDER_FURY = ThunderFurySkill().register()

    fun init() {
        Skill.getTriggers<InitTrigger>().forEach { it.init() }
    }
}