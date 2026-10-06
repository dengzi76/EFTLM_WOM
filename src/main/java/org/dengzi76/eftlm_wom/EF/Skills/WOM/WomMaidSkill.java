package org.dengzi76.eftlm_wom.EF.Skills.WOM;

import org.dengzi76.eftlm_wom.EF.Register.WomSkillDataKeys;

import net.EFTLM.EF.API.Event.MaidSkillInitEvent;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkill;
import net.EFTLM.EF.Skill.MaidSkillBuilder;
import net.EFTLM.EF.Skill.MaidSkillDataManager.SkillDataKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/** Every registered skill is a singleton; all mutable combat state belongs to the maid. */
public abstract class WomMaidSkill extends MaidSkill {
    public static final SkillDataKey<Integer> COOLDOWN = WomSkillDataKeys.COMMON_COOLDOWN;
    public static final SkillDataKey<Integer> TIMER = WomSkillDataKeys.COMMON_TIMER;
    public static final SkillDataKey<Integer> STACK = WomSkillDataKeys.COMMON_STACK;
    public static final SkillDataKey<Integer> LAST_HIT = WomSkillDataKeys.COMMON_LAST_HIT;
    public static final SkillDataKey<Float> HEALTH = WomSkillDataKeys.COMMON_HEALTH;
    public static final SkillDataKey<Float> X = WomSkillDataKeys.COMMON_X;
    public static final SkillDataKey<Float> Y = WomSkillDataKeys.COMMON_Y;
    public static final SkillDataKey<Float> Z = WomSkillDataKeys.COMMON_Z;
    protected final WomSkillCatalog.Entry entry;

    protected WomMaidSkill(MaidSkillBuilder<?> builder, WomSkillCatalog.Entry entry) {
        super(builder);
        this.entry = entry;
    }

    @Override
    public void onInit(MaidSkillInitEvent event) {
        initialize(event.getMaidPatch());
    }

    protected void initialize(MaidPatch<?> patch) {
        WomSkillDataKeys.initialize(patch, this, COOLDOWN);
        WomSkillDataKeys.initialize(patch, this, TIMER);
        WomSkillDataKeys.initialize(patch, this, STACK);
        WomSkillDataKeys.initialize(patch, this, LAST_HIT);
        WomSkillDataKeys.initialize(patch, this, HEALTH, patch.getOriginal().getHealth());
        WomSkillDataKeys.initialize(patch, this, X);
        WomSkillDataKeys.initialize(patch, this, Y);
        WomSkillDataKeys.initialize(patch, this, Z);
    }

    protected int value(MaidPatch<?> patch, SkillDataKey<Integer> key) {
        initialize(patch);
        return patch.getDataValue(this, key);
    }

    protected void set(MaidPatch<?> patch, SkillDataKey<Integer> key, int value) {
        initialize(patch);
        patch.setData(this, key, value);
    }

    protected void tickTimers(MaidPatch<?> patch) {
        initialize(patch);
        if (value(patch, COOLDOWN) > 0) set(patch, COOLDOWN, value(patch, COOLDOWN) - 1);
        if (value(patch, TIMER) > 0) set(patch, TIMER, value(patch, TIMER) - 1);
    }

    @Override
    public ResourceLocation getIcon() {
        return entry.icon();
    }

    @Override
    public boolean canExecute(MaidPatch<?> patch) {
        return super.canExecute(patch) && !patch.CheckState() && entry.matches(patch);
    }

    public void modifyOutgoing(LivingHurtEvent event, MaidPatch<?> patch) { }
    public void modifyIncoming(LivingHurtEvent event, MaidPatch<?> patch) { }
    public void onDefenseSuccess(MaidPatch<?> patch, boolean parry) { }

    protected static int enchantments(LivingEntity entity, Enchantment enchantment) {
        int level = 0;
        for (var stack : entity.getArmorSlots()) level += stack.getEnchantmentLevel(enchantment);
        return level;
    }
}
