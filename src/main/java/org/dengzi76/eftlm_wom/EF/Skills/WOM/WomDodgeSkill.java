package org.dengzi76.eftlm_wom.EF.Skills.WOM;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAttackEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkillBuilder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.phys.Vec3;
import reascer.wom.gameasset.WOMAnimations;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

public class WomDodgeSkill extends WomMaidSkill {
    public WomDodgeSkill(MaidSkillBuilder<?> builder, WomSkillCatalog.Entry entry) { super(builder, entry); }

    @Override
    public void onMaidTick(MaidTickEvent event, MaidPatch<?> patch) {
        if (patch.isLogicalClient()) return;
        tickTimers(patch);
        var maid = patch.getOriginal();
        if (entry.id().equals("time_travel") && value(patch, TIMER) == 1) {
            var pos = new Vec3(patch.getDataValue(this, X), patch.getDataValue(this, Y), patch.getDataValue(this, Z));
            if (WomSkillSupport.safeTeleport(patch, pos)) maid.heal(Math.max(0, patch.getDataValue(this, HEALTH) - maid.getHealth()) * 0.25F);
            set(patch, TIMER, 0);
            set(patch, COOLDOWN, 60);
        }
        if (value(patch, COOLDOWN) > 0 || value(patch, TIMER) > 0 || !WomSkillSupport.ready(patch)
                || !WomSkillSupport.threatened(patch)) return;
        // If multiple dodge books were learned, the first ready skill wins the shared action lock.
        if (!WomSkillSupport.consume(patch, entry.consumption())) return;
        if (entry.id().equals("time_travel")) {
            patch.setData(this, X, (float) maid.getX());
            patch.setData(this, Y, (float) maid.getY());
            patch.setData(this, Z, (float) maid.getZ());
            patch.setData(this, HEALTH, maid.getHealth());
            set(patch, TIMER, 60);
            set(patch, COOLDOWN, 60);
            return;
        }
        int direction = maid.getRandom().nextBoolean() ? 2 : 3;
        WomSkillSupport.play(patch, animation(direction));
        set(patch, TIMER, entry.id().equals("dodge_master") ? 16 : 12);
        set(patch, COOLDOWN, switch (entry.id()) {
            case "punishment_kick" -> 80;
            case "bull_charge" -> 40;
            default -> 12;
        });
    }

    @Override
    public void onMaidAttack(MaidAttackEvent event, MaidPatch<?> patch) {
        if (patch.isLogicalClient() || event.isCanceled() || value(patch, TIMER) <= 0 || entry.id().equals("time_travel")) return;
        var source = event.getSource();
        if (source.getEntity() == null || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(EpicFightDamageTypeTags.BYPASS_DODGE)) return;
        event.setCanceled(true);
        WomSkillSupport.defenseSuccess(patch, false);
        if (entry.id().equals("shadow_step")) WomWeaponSkill.chargeHeld(patch, 2);
        if (entry.id().equals("ender_obscuris")) {
            var target = patch.getTarget();
            if (target != null) {
                Vec3 behind = target.position().subtract(target.getLookAngle().multiply(1, 0, 1).normalize().scale(1.5));
                WomSkillSupport.safeTeleport(patch, behind);
            }
        }
        if (entry.id().equals("precise_roll") || entry.id().equals("dodge_master"))
            patch.setStamina(Math.min(patch.getMaxStamina(), patch.getStamina() + entry.consumption()));
    }

    private AnimationAccessor<? extends StaticAnimation> animation(int direction) {
        boolean left = direction == 2;
        return switch (entry.id()) {
            case "ender_step", "ender_obscuris" -> left ? WOMAnimations.ENDERSTEP_LEFT : WOMAnimations.ENDERSTEP_RIGHT;
            case "shadow_step" -> left ? WOMAnimations.SHADOWSTEP_LEFT : WOMAnimations.SHADOWSTEP_RIGHT;
            case "precise_roll" -> left ? WOMAnimations.KNIGHT_ROLL_LEFT : WOMAnimations.KNIGHT_ROLL_RIGHT;
            case "dodge_master" -> left ? WOMAnimations.DODGEMASTER_LEFT : WOMAnimations.DODGEMASTER_RIGHT;
            case "bull_charge" -> WOMAnimations.RAVANGER_CHARGE;
            case "punishment_kick" -> WOMAnimations.STRONG_KICK;
            default -> WOMAnimations.SHADOWSTEP_BACKWARD;
        };
    }
}
