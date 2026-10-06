package org.dengzi76.eftlm_wom.EF.Skills.WOM;

import org.dengzi76.eftlm_wom.EF.Register.WomSkillDataKeys;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import net.EFTLM.EF.API.Event.MaidHurtTargetEvent;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkillBuilder;
import net.EFTLM.EF.Skill.MaidSkillManager;
import net.EFTLM.EF.Skill.MaidSkillDataManager.SkillDataKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import reascer.wom.gameasset.WOMAnimations;
import reascer.wom.gameasset.animations.weapons.*;
import reascer.wom.world.entity.mob.TargetingHelper;
import reascer.wom.world.entity.mob.EnderHand;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.effect.EpicFightMobEffects;

/** Learnable weapon skills retain their book across weapon changes (EFTLM removes WeaponInnateSkill). */
public class WomWeaponSkill extends WomMaidSkill {
    public static final SkillDataKey<Float> ENERGY = WomSkillDataKeys.WEAPON_ENERGY;
    public static final SkillDataKey<Integer> CHARGES = WomSkillDataKeys.WEAPON_CHARGES;
    public static final SkillDataKey<Integer> COMBO = WomSkillDataKeys.WEAPON_COMBO;

    public WomWeaponSkill(MaidSkillBuilder<?> builder, WomSkillCatalog.Entry entry) { super(builder, entry); }

    @Override
    protected void initialize(MaidPatch<?> patch) {
        super.initialize(patch);
        WomSkillDataKeys.initialize(patch, this, ENERGY);
        WomSkillDataKeys.initialize(patch, this, CHARGES);
        WomSkillDataKeys.initialize(patch, this, COMBO);
    }

    @Override
    public void onMaidTick(MaidTickEvent event, MaidPatch<?> patch) {
        if (patch.isLogicalClient()) return;
        tickTimers(patch);
        var target = patch.getTarget();
        var maid = patch.getOriginal();
        if (value(patch, TIMER) > 0 && (entry.id().equals("true_berserk") || entry.id().equals("regierung"))) {
            if (target != null && value(patch, COOLDOWN) == 0 && WomSkillSupport.ready(patch) && maid.distanceToSqr(target) < 36) {
                int combo = patch.getDataValue(this, COMBO);
                AnimationAccessor<? extends StaticAnimation> animation = switch (entry.id()) {
                    case "true_berserk" -> combo % 2 == 0 ? WOMAnimations.TORMENT_BERSERK_AUTO_1 : WOMAnimations.TORMENT_BERSERK_AUTO_2;
                    default -> combo % 2 == 0 ? AnimsHerrscher.GESETZ_AUTO_1 : AnimsHerrscher.GESETZ_AUTO_2;
                };
                WomSkillSupport.play(patch, animation);
                patch.setData(this, COMBO, combo + 1);
                set(patch, COOLDOWN, 20);
            }
            return;
        }
        if (target == null || !target.isAlive() || !maid.hasLineOfSight(target) || value(patch, COOLDOWN) > 0
                || !WomSkillSupport.ready(patch)) return;
        double range = switch (entry.id()) {
            case "orbital_beam" -> 32;
            case "ender_blast", "ender_fusion" -> 20;
            case "agony_plunge" -> 12;
            default -> 6;
        };
        if (maid.distanceToSqr(target) > range * range) return;
        boolean staminaSkill = entry.id().equals("charybdis") || entry.id().equals("rechargement") || entry.id().equals("regierung");
        if (staminaSkill) {
            if (!WomSkillSupport.consume(patch, entry.consumption())) return;
        } else {
            int charges = patch.getDataValue(this, CHARGES);
            if (charges <= 0) return;
            patch.setData(this, CHARGES, charges - 1);
        }
        execute(patch);
        set(patch, COOLDOWN, switch (entry.id()) {
            case "charybdis" -> 40;
            case "ender_blast", "ender_fusion", "sakura_state" -> 30;
            default -> 100;
        });
    }

    @Override
    public void onHurtTargetPost(MaidHurtTargetEvent.Post event) {
        if (!event.getMaidPatch().isLogicalClient() && event.getAmount() > 0) charge(event.getMaidPatch(), event.getAmount());
    }

    private void charge(MaidPatch<?> patch, float amount) {
        initialize(patch);
        if (entry.id().equals("charybdis") || entry.id().equals("rechargement") || entry.id().equals("regierung")) return;
        float energy = patch.getDataValue(this, ENERGY) + Math.max(0, amount);
        int charges = patch.getDataValue(this, CHARGES);
        while (energy >= entry.consumption() && charges < entry.maxStacks()) {
            energy -= entry.consumption();
            charges++;
        }
        patch.setData(this, ENERGY, Math.min(entry.consumption(), energy));
        patch.setData(this, CHARGES, charges);
    }

    public static void chargeHeld(MaidPatch<?> patch, float amount) {
        for (var id : patch.getLearnedSkills()) {
            var skill = MaidSkillManager.getSkillFor(id);
            if (skill instanceof WomWeaponSkill weapon && weapon.canExecute(patch)) weapon.charge(patch, amount);
        }
    }

    private void execute(MaidPatch<?> patch) {
        var maid = patch.getOriginal();
        int combo = patch.getDataValue(this, COMBO);
        switch (entry.id()) {
            case "charybdis" -> WomSkillSupport.play(patch, WOMAnimations.STAFF_CHARYBDIS);
            case "agony_plunge" -> {
                WomSkillSupport.play(patch, AnimsAgony.AGONY_SKY_DIVE);
                maid.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), 20, 0));
            }
            case "plunder_perdition" -> {
                WomSkillSupport.play(patch, switch (combo % 3) {
                    case 0 -> AnimsRuine.RUINE_PLUNDER;
                    case 1 -> AnimsRuine.RUINE_EXPIATION;
                    default -> AnimsRuine.RUINE_REDEMPTION;
                });
                if (combo % 3 == 0) {
                    for (var enemy : maid.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                            maid.getBoundingBox().inflate(10), e -> WomSkillSupport.enemy(patch, e))) {
                        var hand = new EnderHand((ServerLevel) maid.level(), enemy.position(), maid, enemy);
                        maid.level().addFreshEntity(hand);
                    }
                }
            }
            case "sakura_state" -> WomSkillSupport.play(patch, combo % 2 == 0
                    ? AnimsSatsujin.SATSUJIN_SAKURA_SLASH : AnimsSatsujin.SATSUJIN_SAKURA_SLASH_REVERSE);
            case "ender_blast" -> WomSkillSupport.play(patch, switch (combo % 3) {
                case 0 -> AnimsEnderblaster.ENDERBLASTER_ONEHAND_SHOOT_1;
                case 1 -> AnimsEnderblaster.ENDERBLASTER_ONEHAND_SHOOT_2;
                default -> AnimsEnderblaster.ENDERBLASTER_ONEHAND_SHOOT_3;
            });
            case "ender_fusion" -> WomSkillSupport.play(patch, switch (combo % 4) {
                case 0 -> AnimsEnderblaster.ENDERBLASTER_TWOHAND_SHOOT_1;
                case 1 -> AnimsEnderblaster.ENDERBLASTER_TWOHAND_SHOOT_2;
                case 2 -> AnimsEnderblaster.ENDERBLASTER_TWOHAND_SHOOT_3;
                default -> AnimsEnderblaster.ENDERBLASTER_TWOHAND_SHOOT_4;
            });
            case "true_berserk" -> {
                WomSkillSupport.play(patch, WOMAnimations.TORMENT_BERSERK_CONVERT);
                set(patch, TIMER, 200);
            }
            case "regierung" -> {
                WomSkillSupport.play(patch, AnimsHerrscher.GESETZ_AUTO_1);
                set(patch, TIMER, 1800);
            }
            case "lunar_eclipse" -> {
                WomSkillSupport.play(patch, AnimsMoonless.MOONLESS_LUNAR_ECHO);
                set(patch, TIMER, 100);
            }
            case "solar_arcano" -> {
                WomSkillSupport.play(patch, AnimsSolar.SOLAR_BRASERO);
                set(patch, TIMER, 100);
                if (patch.getTarget() != null) patch.getTarget().setSecondsOnFire(6);
            }
            case "rechargement" -> {
                WomSkillSupport.play(patch, AnimsNapoleon.NAPOLEON_RELOAD_1);
                for (var id : patch.getLearnedSkills()) {
                    var skill = MaidSkillManager.getSkillFor(id);
                    if (skill instanceof WomPassiveSkill passive && id.getPath().equals("napoleon_passive")) passive.set(patch, STACK, 3);
                }
                set(patch, TIMER, 200);
            }
            case "orbital_beam" -> {
                WomSkillSupport.play(patch, AnimsOrbit.ORBIT_LIGHT_BEAM);
                // WOM's animation event only spawns this helper for ServerPlayerPatch.
                var helper = new TargetingHelper((ServerLevel) maid.level(), patch.getTarget().position(), maid, patch.getTarget());
                helper.addTag("wom:saulomonk_beam");
                maid.level().addFreshEntity(helper);
            }
            case "flash_mutilation" -> {
                var target = patch.getTarget();
                var behind = target.position().subtract(target.getLookAngle().multiply(1, 0, 1).normalize().scale(1.5));
                WomSkillSupport.safeTeleport(patch, behind);
                WomSkillSupport.play(patch, AnimsNova.NOVA_FLASH_MUTILATION);
            }
            case "unbreakable" -> {
                set(patch, STACK, 3);
                set(patch, TIMER, 200);
                maid.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), 20, 0));
            }
            default -> throw new IllegalStateException("Missing WOM weapon skill: " + entry.id());
        }
        patch.setData(this, COMBO, combo + 1);
    }

    @Override
    public void modifyIncoming(LivingHurtEvent event, MaidPatch<?> patch) {
        if (entry.id().equals("unbreakable") && value(patch, STACK) > 0 && value(patch, TIMER) > 0) {
            event.setAmount(event.getAmount() * 0.7F);
            WomPassiveSkill.noStun(event);
            set(patch, STACK, value(patch, STACK) - 1);
        }
        if (entry.id().equals("regierung") && value(patch, TIMER) > 0) event.setAmount(event.getAmount() * 0.8F);
    }

    @Override
    public void modifyOutgoing(LivingHurtEvent event, MaidPatch<?> patch) {
        if (value(patch, TIMER) <= 0) return;
        if (entry.id().equals("true_berserk")) event.setAmount(event.getAmount() * 1.5F);
        if (entry.id().equals("rechargement")) event.setAmount(event.getAmount() * 1.2F);
    }

    public static void fireNapoleon(MaidPatch<?> patch) {
        // This WOM attack animation supports MobPatch and creates its own NapoleonBullet.
        WomSkillSupport.play(patch, AnimsNapoleon.NAPOLEON_SHOOT_1);
    }

}
