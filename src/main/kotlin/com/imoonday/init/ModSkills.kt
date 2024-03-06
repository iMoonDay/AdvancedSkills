package com.imoonday.init

import com.imoonday.item.SkillItem
import com.imoonday.skill.*
import com.imoonday.trigger.InitTrigger
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ModSkills {
    @JvmField
    val SKILLS = mutableSetOf<Skill>()

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

    fun init() {
        SKILLS.filterIsInstance<InitTrigger>().forEach { it.init() }
    }

    fun <T : Skill> T.register(): T {
        if (!invalid) Registry.register(Registries.ITEM, id, SkillItem(this))
        SKILLS.add(this)
        return this
    }

    fun get(id: Identifier): Skill = SKILLS.find { it.id == id } ?: EMPTY

    fun getOrNull(id: Identifier): Skill? = SKILLS.find { it.id == id }
}

