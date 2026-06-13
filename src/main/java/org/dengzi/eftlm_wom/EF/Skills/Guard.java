package org.dengzi.eftlm_wom.EF.Skills;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAttackEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Maps;
import net.EFTLM.EF.API.Event.MaidSkillInitEvent;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkill;
import net.EFTLM.EF.Skill.MaidSkillBuilder;
import net.EFTLM.EF.Skill.MaidSkillDataManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import java.util.Map;
import java.util.function.BiFunction;
public class Guard extends MaidSkill {
    public static final MaidSkillDataManager.SkillDataKey<Float> GUARD_PENALTY =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.FLOAT);
    public static final MaidSkillDataManager.SkillDataKey<Integer> GUARD_RESTORE_COUNTER =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.INTEGER);
    public static final MaidSkillDataManager.SkillDataKey<Boolean> Blocking =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.BOOLEAN);
    protected final Map<WeaponCategory, BiFunction<CapabilityItem, MaidPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> guardMotions;
    protected final Map<WeaponCategory, BiFunction<CapabilityItem, MaidPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> guardBreakMotions;
    public static Guard.Builder createGuardBuilder() {
        return new Guard.Builder()
                .addGuardMotion(CapabilityItem.WeaponCategories.AXE, (item, maid) -> Animations.SWORD_GUARD_HIT)
                .addGuardMotion(CapabilityItem.WeaponCategories.GREATSWORD, (item, maid) -> Animations.GREATSWORD_GUARD_HIT)
                .addGuardMotion(CapabilityItem.WeaponCategories.UCHIGATANA, (item, maid) -> Animations.UCHIGATANA_GUARD_HIT)
                .addGuardMotion(CapabilityItem.WeaponCategories.LONGSWORD, (item, maid) -> Animations.LONGSWORD_GUARD_HIT)
                .addGuardMotion(CapabilityItem.WeaponCategories.SPEAR, (item, maid) -> item.getStyle(maid).equals(CapabilityItem.Styles.TWO_HAND) ? Animations.SPEAR_GUARD_HIT : null)
                .addGuardMotion(CapabilityItem.WeaponCategories.SWORD, (item, maid) -> item.getStyle(maid).equals(CapabilityItem.Styles.ONE_HAND) ? Animations.SWORD_GUARD_HIT : Animations.SWORD_DUAL_GUARD_HIT)
                .addGuardMotion(CapabilityItem.WeaponCategories.TACHI, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
                .addGuardBreakMotion(CapabilityItem.WeaponCategories.AXE, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                .addGuardBreakMotion(CapabilityItem.WeaponCategories.GREATSWORD, (item, player) -> Animations.GREATSWORD_GUARD_BREAK)
                .addGuardBreakMotion(CapabilityItem.WeaponCategories.UCHIGATANA, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                .addGuardBreakMotion(CapabilityItem.WeaponCategories.LONGSWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                .addGuardBreakMotion(CapabilityItem.WeaponCategories.SPEAR, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                .addGuardBreakMotion(CapabilityItem.WeaponCategories.SWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                .addGuardBreakMotion(CapabilityItem.WeaponCategories.TACHI, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED);
    }
    public Guard(MaidSkillBuilder builder) {
        super(builder);
        this.guardMotions = Guard.createGuardBuilder().guardMotions;
        this.guardBreakMotions = Guard.createGuardBuilder().guardBreakMotions;
    }
    @Override
    public void onInit(MaidSkillInitEvent event) {
        MaidPatch<?> MaidPatch = event.getMaidPatch();
        MaidPatch.registerData(this,GUARD_PENALTY,0F);
        MaidPatch.registerData(this,GUARD_RESTORE_COUNTER,0);
        MaidPatch.registerData(this,Blocking,false);
    }
    @Override
    public void MaidTick(MaidTickEvent event) {
        EntityMaid Maid = event.getMaid();
        MaidPatch<?> MaidPatch = EpicFightCapabilities.getEntityPatch(Maid, MaidPatch.class);
        if (Maid.level() instanceof ServerLevel) {
            if (MaidPatch != null) {
                if (MaidPatch.getDataValue(this,GUARD_RESTORE_COUNTER) != null) {
                    int Counter = MaidPatch.getDataValue(this,GUARD_RESTORE_COUNTER);
                    MaidPatch.setData(this, GUARD_RESTORE_COUNTER, Counter + 1);
                    if (MaidPatch.getDataValue(this,GUARD_PENALTY) != null) {
                        float Penalty = MaidPatch.getDataValue(this,GUARD_PENALTY);
                        if (Penalty > 0.0F) {
                            if (Maid.tickCount - Counter > 40) {
                                MaidPatch.setData(this,GUARD_PENALTY, 0.0F);
                            }
                        }
                    }
                }
                LivingEntity target = MaidPatch.getTarget();
                if (target != null) {
                    if (MaidPatch.getDataValue(this, Blocking) != null) {
                        LivingEntityPatch<?> targetPatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
                        if (targetPatch != null) {
                            int phase = targetPatch.getEntityState().getLevel();
                            if (phase > 0 && phase < 3) {
                                MaidPatch.setData(this, Blocking, true);
                            } else {
                                MaidPatch.setData(this, Blocking, false);
                            }
                        }
                        double distSqr = Maid.distanceToSqr(target.getX(), target.getY(), target.getZ());
                        boolean isBlocking = MaidPatch.getDataValue(this, Blocking);
                        CapabilityItem ItemCap = MaidPatch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
                        if (ItemCap instanceof WeaponCapability WC) {
                            if (isBlocking && distSqr < 3.0) {
                                if (WC.getLivingMotionModifier(MaidPatch, InteractionHand.MAIN_HAND).get(LivingMotions.BLOCK) != null) {
                                    MaidPatch.playAnimationSynchronized(WC.getLivingMotionModifier(MaidPatch, InteractionHand.MAIN_HAND).get(LivingMotions.BLOCK), 0F);
                                }
                                Maid.getNavigation().stop();
                                Maid.getMoveControl().strafe(-1.0F, 0.0F);
                            }
                        }
                    }
                }
            }
        }
    }
    @Override
    public void MaidAttack(MaidAttackEvent event) {
        if (!(event.getMaid().level() instanceof ServerLevel level)) return;
        MaidPatch<?> maidPatch = EpicFightCapabilities.getEntityPatch(event.getMaid(), MaidPatch.class);
        if (maidPatch == null) return;
        boolean isBlocking = maidPatch.getDataValue(this, Blocking);
        if (!isBlocking) return;
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        if (attacker != null) {
            Vec3 lookVec = event.getMaid().getLookAngle();
            Vec3 toAttacker = attacker.position().subtract(event.getMaid().position()).normalize();
            double dot = lookVec.dot(toAttacker);
            if (dot < 0.3) {
                return;
            }
            float damage = event.getAmount();
            float penalty = maidPatch.getDataValue(this, GUARD_PENALTY) + damage * 0.5F;
            maidPatch.setData(this, GUARD_PENALTY, penalty);
            maidPatch.setData(this, GUARD_RESTORE_COUNTER, event.getMaid().tickCount);
            event.setCanceled(true);
            maidPatch.playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
            EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(level, HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, event.getMaid(), event.getSource().getDirectEntity());
            playGuardAnimation(event.getMaid(), maidPatch);
        }
    }
    public static class Builder extends MaidSkillBuilder {
        protected final Map<WeaponCategory, BiFunction<CapabilityItem, MaidPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> guardMotions = Maps.newHashMap();
        protected final Map<WeaponCategory, BiFunction<CapabilityItem, MaidPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> guardBreakMotions = Maps.newHashMap();
        public Builder() {
        }
        public Guard.Builder addGuardMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, MaidPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>> function) {
            this.guardMotions.put(weaponCategory, function);
            return this;
        }
        public Guard.Builder addGuardBreakMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, MaidPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>> function) {
            this.guardBreakMotions.put(weaponCategory, function);
            return this;
        }
    }
    private void playGuardAnimation(EntityMaid maid, MaidPatch<?> maidPatch) {
        CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(maid.getMainHandItem());
        WeaponCategory category = itemCap.getWeaponCategory();
        BiFunction<CapabilityItem, MaidPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>> func = this.guardMotions.get(category);
        if (func != null) {
            AnimationManager.AnimationAccessor<? extends StaticAnimation> animAccessor = func.apply(itemCap, maidPatch);
            if (animAccessor != null) {
                maidPatch.playAnimationSynchronized(animAccessor,0F);
            }
        }
    }

}
