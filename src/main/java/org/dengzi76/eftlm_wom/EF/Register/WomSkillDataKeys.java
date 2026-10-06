package org.dengzi76.eftlm_wom.EF.Register;

import java.util.List;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkill;
import net.EFTLM.EF.Skill.MaidSkillDataKeys;
import net.EFTLM.EF.Skill.MaidSkillDataManager.SkillDataKey;
import net.EFTLM.EF.Skill.MaidSkillDataManager.ValueType;
import net.minecraft.resources.ResourceLocation;
import org.dengzi76.eftlm_wom.eftlm_wom;

/** Global key definitions; mutable values remain in each maid's per-skill data map. */
public final class WomSkillDataKeys {
    public static final SkillDataKey<Integer> COMMON_COOLDOWN = create("wommaidskill_cooldown", ValueType.integer(), 0);
    public static final SkillDataKey<Integer> COMMON_TIMER = create("wommaidskill_timer", ValueType.integer(), 0);
    public static final SkillDataKey<Integer> COMMON_STACK = create("wommaidskill_stack", ValueType.integer(), 0);
    public static final SkillDataKey<Integer> COMMON_LAST_HIT = create("wommaidskill_last_hit", ValueType.integer(), -10000);
    public static final SkillDataKey<Float> COMMON_HEALTH = create("wommaidskill_health", ValueType.floatType(), 0F);
    public static final SkillDataKey<Float> COMMON_X = create("wommaidskill_x", ValueType.floatType(), 0F);
    public static final SkillDataKey<Float> COMMON_Y = create("wommaidskill_y", ValueType.floatType(), 0F);
    public static final SkillDataKey<Float> COMMON_Z = create("wommaidskill_z", ValueType.floatType(), 0F);
    public static final SkillDataKey<Float> WEAPON_ENERGY = create("womweaponskill_energy", ValueType.floatType(), 0F);
    public static final SkillDataKey<Integer> WEAPON_CHARGES = create("womweaponskill_charges", ValueType.integer(), 0);
    public static final SkillDataKey<Integer> WEAPON_COMBO = create("womweaponskill_combo", ValueType.integer(), 0);
    public static final SkillDataKey<Float> GUARD_PENALTY = create("guard_guard_penalty", ValueType.floatType(), 0F);
    public static final SkillDataKey<Integer> GUARD_RESTORE_COUNTER = create("guard_guard_restore_counter", ValueType.integer(), 0);
    public static final SkillDataKey<Boolean> GUARD_BLOCKING = create("guard_blocking", ValueType.booleanType(), false);
    public static final SkillDataKey<Boolean> MEDITATION_MEDITATING = create("meditation_meditating", ValueType.booleanType(), false);
    public static final SkillDataKey<Integer> MEDITATION_TIMER = create("meditation_meditation_timer", ValueType.integer(), 0);
    public static final SkillDataKey<Integer> MEDITATION_CURRENT_STAGE = create("meditation_current_stage", ValueType.integer(), 0);
    public static final SkillDataKey<Float> MEDITATION_LAST_POS_X = create("meditation_last_pos_x", ValueType.floatType(), 0F);
    public static final SkillDataKey<Float> MEDITATION_LAST_POS_Z = create("meditation_last_pos_z", ValueType.floatType(), 0F);
    public static final SkillDataKey<Boolean> MEDITATION_ANIMATION_ACTIVE = create("meditation_animation_active", ValueType.booleanType(), false);
    public static final SkillDataKey<Integer> MEDITATION_DUREE = create("meditation_duree", ValueType.integer(), 0);
    public static final SkillDataKey<Integer> MEDITATION_CYCLE = create("meditation_cycle", ValueType.integer(), 0);
    public static final SkillDataKey<Integer> MEDITATION_COOLDOWN_END_TICK = create("meditation_cooldown_end_tick", ValueType.integer(), 0);

    public static final List<SkillDataKey<?>> ALL = List.of(
            COMMON_COOLDOWN,
            COMMON_TIMER,
            COMMON_STACK,
            COMMON_LAST_HIT,
            COMMON_HEALTH,
            COMMON_X,
            COMMON_Y,
            COMMON_Z,
            WEAPON_ENERGY,
            WEAPON_CHARGES,
            WEAPON_COMBO,
            GUARD_PENALTY,
            GUARD_RESTORE_COUNTER,
            GUARD_BLOCKING,
            MEDITATION_MEDITATING,
            MEDITATION_TIMER,
            MEDITATION_CURRENT_STAGE,
            MEDITATION_LAST_POS_X,
            MEDITATION_LAST_POS_Z,
            MEDITATION_ANIMATION_ACTIVE,
            MEDITATION_DUREE,
            MEDITATION_CYCLE,
            MEDITATION_COOLDOWN_END_TICK);

    private WomSkillDataKeys() { }

    private static <T> SkillDataKey<T> create(String path, ValueType<T> type, T defaultValue) {
        return MaidSkillDataKeys.create(ResourceLocation.fromNamespaceAndPath(eftlm_wom.MODID, path), type, defaultValue);
    }

    /** Force registration before skills and saved maid data are loaded. */
    public static void register() {
        for (var key : ALL) {
            if (SkillDataKey.byId(key.getId()) != key)
                throw new IllegalStateException("Unregistered maid skill data key: " + key.getId());
        }
    }

    public static <T> void initialize(MaidPatch<?> patch, MaidSkill skill, SkillDataKey<T> key) {
        initialize(patch, skill, key, key.getDefaultValue());
    }

    /** Fill missing fields without overwriting values restored from the save. */
    public static <T> void initialize(MaidPatch<?> patch, MaidSkill skill, SkillDataKey<T> key, T initialValue) {
        if (!patch.hasData(skill, key)) patch.registerData(skill, key, initialValue);
    }
}
