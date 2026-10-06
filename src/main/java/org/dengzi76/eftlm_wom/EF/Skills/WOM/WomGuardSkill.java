package org.dengzi76.eftlm_wom.EF.Skills.WOM;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAttackEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkillBuilder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

public class WomGuardSkill extends WomMaidSkill {
    public WomGuardSkill(MaidSkillBuilder<?> builder, WomSkillCatalog.Entry entry) { super(builder, entry); }

    @Override
    public void onMaidTick(MaidTickEvent event, MaidPatch<?> patch) {
        if (patch.isLogicalClient()) return;
        tickTimers(patch);
        if (entry.id().equals("soul_protection")) {
            if (value(patch, COOLDOWN) == 0) {
                set(patch, STACK, Math.min(30, value(patch, STACK) + 1));
                set(patch, COOLDOWN, 12);
            }
            return;
        }
        if (entry.id().equals("shulker_cloak")) {
            if (value(patch, COOLDOWN) == 0) {
                set(patch, STACK, Math.min(5, value(patch, STACK) + 1));
                set(patch, COOLDOWN, 60);
            }
            return;
        }
        var cap = patch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        if (!(cap instanceof WeaponCapability) || value(patch, COOLDOWN) > 0
                || !WomSkillSupport.ready(patch) || !WomSkillSupport.threatened(patch)) return;
        var block = cap.getLivingMotionModifier(patch, InteractionHand.MAIN_HAND).get(LivingMotions.BLOCK);
        if (block == null) return;
        WomSkillSupport.play(patch, block);
        set(patch, TIMER, 14);
        set(patch, COOLDOWN, 18);
    }

    @Override
    public void onMaidAttack(MaidAttackEvent event, MaidPatch<?> patch) {
        if (patch.isLogicalClient() || event.isCanceled() || value(patch, TIMER) <= 0) return;
        var source = event.getSource();
        var position = source.getSourcePosition();
        if (position == null || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(EpicFightDamageTypeTags.GUARD_PUNCTURE)) return;
        var maid = patch.getOriginal();
        if (maid.getLookAngle().dot(position.subtract(maid.position()).normalize()) <= 0) return;
        float cost = switch (entry.id()) {
            case "perfect_bulwark" -> event.getAmount() * 0.2F;
            case "vengeful_parry" -> 8;
            case "buster_parade" -> event.getAmount() * 0.5F;
            case "counter_attack" -> event.getAmount() * 4;
            default -> throw new IllegalStateException("Missing WOM parry skill: " + entry.id());
        };
        if (!WomSkillSupport.consume(patch, cost)) return;
        event.setCanceled(true);
        set(patch, TIMER, 0);
        patch.playSound(EpicFightSounds.CLASH.get(), 0F, 0F);
        WomSkillSupport.defenseSuccess(patch, true);
        var cap = patch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        var category = cap.getWeaponCategory();
        if (entry.id().equals("perfect_bulwark")) {
            patch.setStamina(Math.min(patch.getMaxStamina(), patch.getStamina() + cost));
            WomSkillSupport.play(patch, Animations.SWORD_GUARD_HIT);
        } else if (entry.id().equals("vengeful_parry")) {
            WomSkillSupport.play(patch, reascer.wom.gameasset.WOMAnimations.STRONG_PUNCH);
        } else if (entry.id().equals("buster_parade")) {
            WomSkillSupport.play(patch, Animations.GREATSWORD_DASH);
        } else {
            var counter = category == CapabilityItem.WeaponCategories.GREATSWORD ? Animations.GREATSWORD_DASH
                    : category == CapabilityItem.WeaponCategories.LONGSWORD ? Animations.LONGSWORD_DASH
                    : category == CapabilityItem.WeaponCategories.UCHIGATANA ? Animations.UCHIGATANA_SHEATHING_DASH
                    : category == CapabilityItem.WeaponCategories.SPEAR ? Animations.GRASPING_SPIRAL_SECOND
                    : category == CapabilityItem.WeaponCategories.TACHI ? Animations.RUSHING_TEMPO2 : Animations.SWEEPING_EDGE;
            WomSkillSupport.play(patch, counter);
        }
    }

    @Override
    public void modifyIncoming(LivingHurtEvent event, MaidPatch<?> patch) {
        if (event.getSource().getEntity() == patch.getOriginal() || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (entry.id().equals("soul_protection")) {
            set(patch, STACK, Math.max(0, value(patch, STACK) - 3));
            set(patch, COOLDOWN, 12);
            event.setAmount(event.getAmount() * (0.8F - value(patch, STACK) * 0.01F));
        } else if (entry.id().equals("shulker_cloak")) {
            if (value(patch, STACK) > 0) {
                set(patch, STACK, value(patch, STACK) - 1);
                event.setAmount(event.getAmount() * 0.4F);
            }
            set(patch, COOLDOWN, 60);
        }
    }
}
