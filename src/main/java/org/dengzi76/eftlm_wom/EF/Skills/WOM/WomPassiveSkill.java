package org.dengzi76.eftlm_wom.EF.Skills.WOM;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import net.EFTLM.EF.API.Event.MaidHurtTargetEvent;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkillBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import reascer.wom.gameasset.WOMAnimations;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.effect.EpicFightMobEffects;

/** Passive and identity mechanics rewritten against maid events, without player containers. */
public class WomPassiveSkill extends WomMaidSkill {
    public WomPassiveSkill(MaidSkillBuilder<?> builder, WomSkillCatalog.Entry entry) { super(builder, entry); }

    @Override
    public void onMaidTick(MaidTickEvent event, MaidPatch<?> patch) {
        if (patch.isLogicalClient()) return;
        tickTimers(patch);
        var maid = patch.getOriginal();
        var target = patch.getTarget();
        switch (entry.id()) {
            case "heart_shield" -> {
                if (patch.getEntityState().inaction()) set(patch, COOLDOWN, 60);
                int interval = Math.max(1, 30 / (1 + enchantments(maid, Enchantments.ALL_DAMAGE_PROTECTION) / 4));
                if (value(patch, COOLDOWN) == 0 && maid.tickCount % interval == 0)
                    maid.setAbsorptionAmount(Math.min(20, maid.getAbsorptionAmount() + 1));
            }
            case "inner_growth" -> WomWeaponSkill.chargeHeld(patch,
                    0.05F + enchantments(maid, Enchantments.BLAST_PROTECTION) / 400F);
            case "lethal_focus" -> {
                if (value(patch, TIMER) == 0 && value(patch, STACK) > 0) set(patch, STACK, value(patch, STACK) - 1);
            }
            case "lunatic_vivacity" -> {
                int timer = value(patch, TIMER);
                if (target != null) set(patch, TIMER, timer <= 0 ? 600 : Math.max(0, timer - (timer < 200 ? 1 : 0)));
            }
            case "voodoo_magic" -> {
                float previous = patch.getDataValue(this, HEALTH);
                if (maid.getHealth() < previous) restoreStamina(patch, (previous - maid.getHealth()) * 0.5F);
                if (target != null && patch.getStamina() < patch.getMaxStamina() && value(patch, COOLDOWN) == 0
                        && maid.getHealth() > 0.5F) {
                    maid.setHealth(Math.max(0.5F, maid.getHealth() - 0.5F));
                    restoreStamina(patch, 1.5F);
                    set(patch, COOLDOWN, 20);
                }
                patch.setData(this, HEALTH, maid.getHealth());
            }
            case "proximity_exploiter" -> {
                int enemies = maid.level().getEntitiesOfClass(LivingEntity.class, maid.getBoundingBox().inflate(10),
                        e -> maid.distanceToSqr(e) <= 100 && WomSkillSupport.enemy(patch, e)).size();
                if (enemies > 0) WomWeaponSkill.chargeHeld(patch, 0.15F + enemies * 0.05F);
            }
            case "all_eyes_on_me", "all_eyes_on_you" -> {
                if (value(patch, TIMER) > 0) {
                    boolean offense = entry.id().equals("all_eyes_on_me");
                    if (offense) maid.heal(0.25F);
                    restoreStamina(patch, offense ? 0.3F : 0.4F);
                    WomWeaponSkill.chargeHeld(patch, offense ? 0.5F : 1F);
                }
            }
            case "avatar_of_might" -> {
                if (!patch.getEntityState().inaction() && value(patch, COOLDOWN) == 0)
                    set(patch, STACK, Math.min(24, value(patch, STACK) + 2));
                else set(patch, STACK, 0);
            }
            case "back_and_forth" -> {
                if (target != null && maid.distanceToSqr(target) >= 144) set(patch, STACK, 1);
                if (target != null && maid.distanceToSqr(target) <= 9 && value(patch, STACK) > 0
                        && WomSkillSupport.ready(patch)) {
                    WomSkillSupport.play(patch, WOMAnimations.STRONG_PUNCH);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                    set(patch, STACK, 0);
                    set(patch, COOLDOWN, 40);
                }
            }
            case "shooting_style" -> {
                var item = maid.getMainHandItem();
                boolean unarmed = item.isEmpty() || item.getItem() instanceof BowItem
                        || patch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getWeaponCategory() == CapabilityItem.WeaponCategories.FIST;
                if (unarmed && target != null && maid.distanceToSqr(target) < 9 && value(patch, COOLDOWN) == 0
                        && WomSkillSupport.ready(patch)) {
                    var animation = switch (value(patch, STACK) % 3) {
                        case 0 -> WOMAnimations.KICK_AUTO_1;
                        case 1 -> WOMAnimations.KICK_AUTO_2;
                        default -> WOMAnimations.KICK_AUTO_3;
                    };
                    WomSkillSupport.play(patch, animation);
                    set(patch, STACK, (value(patch, STACK) + 1) % 3);
                    set(patch, COOLDOWN, 20);
                }
            }
            case "aqua_maneuvre" -> {
                if (maid.isInWater()) {
                    maid.setAirSupply(maid.getMaxAirSupply());
                    maid.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 25, 0, false, false));
                }
            }
            case "spider_techniques" -> {
                if (maid.horizontalCollision && target != null && target.getY() > maid.getY() && !maid.isInWater()) {
                    var movement = maid.getDeltaMovement();
                    maid.setDeltaMovement(movement.x, Math.max(movement.y, 0.2), movement.z);
                    maid.fallDistance = 0;
                }
            }
            case "satsujin_passive" -> {
                if (!patch.getEntityState().inaction()) WomWeaponSkill.chargeHeld(patch, target == null ? 0.5F : 0.15F);
            }
            case "ruine_passive" -> {
                if (value(patch, TIMER) > 0) restoreStamina(patch, 0.15F);
            }
            case "lunar_echo_passive" -> {
                if (!patch.getEntityState().inaction()) WomWeaponSkill.chargeHeld(patch, 0.2F);
            }
            case "torment_passive" -> {
                if (target != null && !patch.getEntityState().inaction()) set(patch, STACK, Math.min(20, value(patch, STACK) + 2));
            }
            case "solar_passive" -> {
                if (target != null) set(patch, STACK, Math.min(100, value(patch, STACK) + 2));
                else set(patch, STACK, Math.max(0, value(patch, STACK) - 1));
            }
            case "napoleon_passive" -> {
                if (target != null && value(patch, COOLDOWN) == 0 && value(patch, STACK) > 0
                        && maid.distanceToSqr(target) < 256 && maid.hasLineOfSight(target) && WomSkillSupport.ready(patch)) {
                    WomWeaponSkill.fireNapoleon(patch);
                    set(patch, STACK, value(patch, STACK) - 1);
                    set(patch, COOLDOWN, 12);
                }
            }
            case "unbreakable_passive" -> {
                if (value(patch, TIMER) > 0) maid.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), 5, 0, false, false));
            }
            default -> { }
        }
    }

    @Override
    public void modifyOutgoing(LivingHurtEvent event, MaidPatch<?> patch) {
        var maid = patch.getOriginal();
        float multiplier = switch (entry.id()) {
            case "adrenaline" -> maid.getHealth() < maid.getMaxHealth() * 0.35F ? 1.6F : 1;
            case "mindset" -> maid.getHealth() > maid.getMaxHealth() * 0.7F ? 1.3F : 1;
            case "latent_retribution" -> value(patch, TIMER) > 0 ? 1.4F : 1;
            case "dancing_blade" -> value(patch, STACK) >= 2 ? 1.8F : 1;
            case "lethal_focus" -> 1 + value(patch, STACK) / 100F;
            case "critical_knowledge" -> {
                float chance = Math.min(1F, 0.4F + enchantments(maid, Enchantments.FIRE_PROTECTION) * 0.05F);
                yield maid.getRandom().nextFloat() < chance ? 2 + enchantments(maid, Enchantments.BLAST_PROTECTION) * 0.15F : 1;
            }
            case "torment_passive" -> value(patch, STACK) >= 20 ? 3 : 1;
            default -> 1;
        };
        event.setAmount(event.getAmount() * multiplier);
    }

    @Override
    public void onHurtTargetPost(MaidHurtTargetEvent.Post event) {
        var patch = event.getMaidPatch();
        if (patch.isLogicalClient() || event.getAmount() <= 0) return;
        initialize(patch);
        var maid = patch.getOriginal();
        var target = event.getTarget();
        switch (entry.id()) {
            case "vampirize" -> maid.heal(event.getAmount() * 0.2F);
            case "dancing_blade" -> {
                if (newAttack(patch, event)) set(patch, STACK, (value(patch, STACK) + 1) % 3);
            }
            case "lethal_focus" -> {
                set(patch, STACK, Math.min(80 + 10 * enchantments(maid, Enchantments.BLAST_PROTECTION),
                        value(patch, STACK) + 6 + enchantments(maid, Enchantments.FIRE_PROTECTION)));
                set(patch, TIMER, 60);
            }
            case "dopamine" -> {
                if (value(patch, COOLDOWN) == 0 && event.getSource() instanceof EpicFightDamageSource source
                        && source.getAnimation().get().getRegistryName().getPath().matches(".*(dash|bypass|krummen|austerlitz|pistolero).*")) {
                    restoreStamina(patch, event.getAmount() * 2);
                    set(patch, COOLDOWN, 40);
                }
            }
            case "all_eyes_on_me" -> {
                if (value(patch, TIMER) == 0 && event.getSource() instanceof EpicFightDamageSource source)
                    remember(patch, source.getAnimation().get().getRegistryName().toString(), 4, 80);
            }
            case "evil_tachi_passive" -> {
                var wither = target.getEffect(MobEffects.WITHER);
                if (wither != null) {
                    maid.heal(1);
                    int remaining = wither.getDuration() - 20;
                    target.removeEffect(MobEffects.WITHER);
                    if (remaining > 0) target.addEffect(new MobEffectInstance(MobEffects.WITHER, remaining, wither.getAmplifier()));
                }
                int sweeping = maid.getMainHandItem().getEnchantmentLevel(Enchantments.SWEEPING_EDGE);
                if (maid.getRandom().nextFloat() < Math.min(1F, 0.4F + 0.1F * sweeping))
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 120,
                            Math.min(4, wither == null ? 0 : wither.getAmplifier() + 1)));
            }
            case "herrscher_passive" -> set(patch, STACK, Math.min(50, value(patch, STACK) + 2));
            case "ruine_passive" -> set(patch, TIMER, 140);
            case "solar_passive" -> { if (value(patch, STACK) > 20) target.setSecondsOnFire(4); }
            case "torment_passive" -> { if (value(patch, STACK) >= 20) set(patch, STACK, 0); }
            case "unbreakable_passive" -> set(patch, TIMER, 40);
            default -> { }
        }
    }

    @Override
    public void modifyIncoming(LivingHurtEvent event, MaidPatch<?> patch) {
        var maid = patch.getOriginal();
        switch (entry.id()) {
            case "adrenaline" -> { if (maid.getHealth() < maid.getMaxHealth() * 0.35F) event.setAmount(event.getAmount() * 0.6F); }
            case "mindset" -> { if (maid.getHealth() > maid.getMaxHealth() * 0.7F) event.setAmount(event.getAmount() * 0.8F); }
            case "arrow_tenacity" -> {
                if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) noStun(event);
            }
            case "pain_anticipation", "latent_retribution" -> {
                if (value(patch, COOLDOWN) == 0 || value(patch, TIMER) > 0) {
                    boolean pain = entry.id().equals("pain_anticipation");
                    if (pain) event.setAmount(event.getAmount() * 0.6F);
                    noStun(event);
                    set(patch, TIMER, WomSkillSupport.learned(patch, pain ? "latent_retribution" : "pain_anticipation") ? 80 : 40);
                    set(patch, COOLDOWN, pain || WomSkillSupport.learned(patch, "pain_anticipation") ? 30 : 50);
                }
            }
            case "heart_shield" -> set(patch, COOLDOWN, 60);
            case "herrscher_passive" -> {
                event.setAmount(event.getAmount() * (1 - value(patch, STACK) / 100F));
                set(patch, STACK, Math.max(0, value(patch, STACK) - 5));
            }
            case "avatar_of_might" -> {
                if (value(patch, STACK) >= 24 && value(patch, COOLDOWN) == 0 && WomSkillSupport.consume(patch, 12)) {
                    noStun(event);
                    maid.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), 100, 0));
                    WomSkillSupport.play(patch, WOMAnimations.AVATAR_OF_MIGHT);
                    for (var enemy : maid.level().getEntitiesOfClass(LivingEntity.class, maid.getBoundingBox().inflate(10), e -> WomSkillSupport.enemy(patch, e)))
                        enemy.knockback(1, maid.getX() - enemy.getX(), maid.getZ() - enemy.getZ());
                    set(patch, STACK, 0);
                    set(patch, COOLDOWN, 80);
                }
            }
            default -> { }
        }
    }

    @Override
    public void onDefenseSuccess(MaidPatch<?> patch, boolean parry) {
        if (entry.id().equals("manipulator") && parry) restoreStamina(patch, (patch.getMaxStamina() - patch.getStamina()) * 0.6F);
        if (entry.id().equals("all_eyes_on_you") && value(patch, TIMER) == 0 && patch.getTarget() != null) {
            var targetPatch = yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(patch.getTarget(), yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch.class);
            String key = targetPatch == null ? patch.getTarget().getType().toString() : targetPatch.getServerAnimator().animationPlayer.getRealAnimation().toString();
            remember(patch, key + (parry ? "B" : "D"), 3, 160);
        }
    }

    private boolean newAttack(MaidPatch<?> patch, MaidHurtTargetEvent.Post event) {
        // Multi-target hits in the same tick must not advance the combo more than once.
        if (value(patch, LAST_HIT) == patch.getOriginal().tickCount) return false;
        set(patch, LAST_HIT, patch.getOriginal().tickCount);
        return true;
    }

    private void remember(MaidPatch<?> patch, String animation, int required, int duration) {
        CompoundTag data = patch.getOriginal().getPersistentData();
        String key = "eftlm_wom:" + entry.id();
        CompoundTag memory = data.getCompound(key);
        if (memory.contains(animation)) return;
        memory.putBoolean(animation, true);
        if (memory.getAllKeys().size() >= required) {
            memory = new CompoundTag();
            set(patch, TIMER, duration);
        }
        data.put(key, memory);
    }

    private static void restoreStamina(MaidPatch<?> patch, float amount) {
        patch.setStamina(Math.min(patch.getMaxStamina(), patch.getStamina() + amount));
    }

    public static void noStun(LivingHurtEvent event) {
        if (event.getSource() instanceof EpicFightDamageSource source) { source.setStunType(StunType.NONE); source.setBaseImpact(0); }
    }
}
