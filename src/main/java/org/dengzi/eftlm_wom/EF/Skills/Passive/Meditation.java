package org.dengzi.eftlm_wom.EF.Skills.Passive;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.EFTLM.EF.API.Event.MaidSkillInitEvent;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkill;
import net.EFTLM.EF.Skill.MaidSkillBuilder;
import net.EFTLM.EF.Skill.MaidSkillDataManager;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import reascer.wom.gameasset.WOMAnimations;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class Meditation extends MaidSkill {
    public static final MaidSkillDataManager.SkillDataKey<Boolean> MEDITATING =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.BOOLEAN);
    public static final MaidSkillDataManager.SkillDataKey<Integer> MEDITATION_TIMER =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.INTEGER);
    public static final MaidSkillDataManager.SkillDataKey<Integer> CURRENT_STAGE =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.INTEGER);
    public static final MaidSkillDataManager.SkillDataKey<Float> LAST_POS_X =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.FLOAT);
    public static final MaidSkillDataManager.SkillDataKey<Float> LAST_POS_Z =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.FLOAT);
    public static final MaidSkillDataManager.SkillDataKey<Boolean> ANIMATION_ACTIVE =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.BOOLEAN);
    public static final MaidSkillDataManager.SkillDataKey<Integer> DUREE =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.INTEGER);
    public static final MaidSkillDataManager.SkillDataKey<Integer> CYCLE =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.INTEGER);
    public static final MaidSkillDataManager.SkillDataKey<Integer> COOLDOWN_END_TICK =
            MaidSkillDataManager.SkillDataKey.createDataKey(MaidSkillDataManager.SkillDataKey.INTEGER);

    private static final int MEDITATION_COOLDOWN = 100;
    private static final int ANIMATION_START_TICK = 200;
    private static final int STAGE_1_TICKS = 400;
    private static final int STAGE_2_TICKS = 800;
    private static final int STAGE_3_TICKS = 1200;
    private static final int STAGE_4_TICKS = 6000;

    private static final DustParticleOptions RED_PARTICLES = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.5F);
    private static final DustParticleOptions CYAN_PARTICLES = new DustParticleOptions(new Vector3f(0.0F, 1.0F, 1.0F), 1.5F);
    private static final DustParticleOptions YELLOW_PARTICLES = new DustParticleOptions(new Vector3f(1.0F, 1.0F, 0.4F), 1.5F);
    private static final DustParticleOptions PURPLE_PARTICLES = new DustParticleOptions(new Vector3f(0.5F, 0.0F, 0.5F), 1.5F);

    public Meditation(MaidSkillBuilder builder) {
        super(builder);
    }

    @Override
    public void onInit(MaidSkillInitEvent event) {
        MaidPatch<?> patch = event.getMaidPatch();
        patch.registerData(this, MEDITATING, false);
        patch.registerData(this, MEDITATION_TIMER, 0);
        patch.registerData(this, CURRENT_STAGE, 0);
        patch.registerData(this, LAST_POS_X, 0.0F);
        patch.registerData(this, LAST_POS_Z, 0.0F);
        patch.registerData(this, ANIMATION_ACTIVE, false);
        patch.registerData(this, DUREE, 0);
        patch.registerData(this, CYCLE, 0);
        patch.registerData(this, COOLDOWN_END_TICK, 0);
    }

    @Override
    public void onMaidTick(MaidTickEvent event, MaidPatch<?> patch) {
        EntityMaid maid = event.getMaid();
        if (!(maid.level() instanceof ServerLevel level)) return;

        Boolean isMeditating = patch.getDataValue(this, MEDITATING);
        Integer timer = patch.getDataValue(this, MEDITATION_TIMER);
        Integer stage = patch.getDataValue(this, CURRENT_STAGE);
        Boolean animActive = patch.getDataValue(this, ANIMATION_ACTIVE);
        Integer duree = patch.getDataValue(this, DUREE);
        Integer cycle = patch.getDataValue(this, CYCLE);

        if (isMeditating == null || timer == null || stage == null || animActive == null || duree == null || cycle == null) return;

        CapabilityItem itemCap = patch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        boolean hasWeapon = itemCap instanceof WeaponCapability;

        if (!hasWeapon) {
            if (isMeditating) {
                stopMeditating(maid, patch);
            }
            spawnParticles(maid, level, stage);
            return;
        }

        if (isMeditating) {
            Float lastX = patch.getDataValue(this, LAST_POS_X);
            Float lastZ = patch.getDataValue(this, LAST_POS_Z);
            if (lastX != null && lastZ != null) {
                double dx = maid.getX() - lastX;
                double dz = maid.getZ() - lastZ;
                if (dx * dx + dz * dz > 0.01) {
                    stopMeditating(maid, patch);
                    spawnParticles(maid, level, stage);
                    return;
                }
            }
        }

        LivingEntity target = patch.getTarget();
        if (target != null) {
            if (isMeditating) {
                stopMeditating(maid, patch);
            }
            spawnParticles(maid, level, stage);
            return;
        }

        if (!isMeditating) {
            Integer cooldownEnd = patch.getDataValue(this, COOLDOWN_END_TICK);
            if (cooldownEnd != null && maid.tickCount - cooldownEnd < MEDITATION_COOLDOWN) {
                spawnParticles(maid, level, stage);
                return;
            }

            patch.setData(this, MEDITATING, true);
            patch.setData(this, MEDITATION_TIMER, 0);
            patch.setData(this, CURRENT_STAGE, 0);
            patch.setData(this, LAST_POS_X, (float) maid.getX());
            patch.setData(this, LAST_POS_Z, (float) maid.getZ());
            patch.setData(this, ANIMATION_ACTIVE, false);
            patch.setData(this, DUREE, 0);
            patch.setData(this, CYCLE, 0);
            removeStageEffects(maid);
            timer = 0;
            stage = 0;
        }

        int newTimer = timer + 1;
        patch.setData(this, MEDITATION_TIMER, newTimer);

        if (newTimer == ANIMATION_START_TICK && !animActive) {
            patch.playAnimationSynchronized(WOMAnimations.MEDITATION_SITING, 0.0F);
            patch.setData(this, ANIMATION_ACTIVE, true);
            patch.setData(this, DUREE, newTimer * 6);
            patch.setData(this, CYCLE, 30);
            duree = newTimer * 6;
            cycle = 30;
        }

        if (animActive && duree > 0) {
            int newDuree = duree - 1;
            patch.setData(this, DUREE, newDuree);

            if (newDuree <= 0) {
                patch.setData(this, ANIMATION_ACTIVE, false);
            }
        }

        int newStage = stage;
        if (newTimer >= STAGE_4_TICKS) {
            newStage = 4;
        } else if (newTimer >= STAGE_3_TICKS) {
            newStage = 3;
        } else if (newTimer >= STAGE_2_TICKS) {
            newStage = 2;
        } else if (newTimer >= STAGE_1_TICKS) {
            newStage = 1;
        }

        if (newStage != stage) {
            patch.setData(this, CURRENT_STAGE, newStage);
            applyStageEffects(maid, patch, newStage);

            if ((newStage == 3 || newStage == 4) && animActive) {
                patch.setData(this, DUREE, newTimer * 6);
                duree = newTimer * 6;
            }
        }

        if (animActive && duree > 0) {
            int newCycle = cycle - 1;
            patch.setData(this, CYCLE, newCycle);
            if (newCycle <= 0) {
                patch.playAnimationSynchronized(WOMAnimations.MEDITATION_BREATHING, 0.0F);
                patch.setData(this, CYCLE, 80);
            }
        }

        if (newStage == 3 || newStage == 4) {
            if (animActive && duree > 0) {
                maid.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 110, 0, false, false));
            }
        }

        spawnParticles(maid, level, newStage);
    }

    private void stopMeditating(EntityMaid maid, MaidPatch<?> patch) {
        patch.setData(this, MEDITATING, false);
        patch.setData(this, ANIMATION_ACTIVE, false);
        patch.setData(this, DUREE, 0);
        patch.setData(this, CYCLE, 0);
        patch.setData(this, COOLDOWN_END_TICK, maid.tickCount);
        patch.resetAnimation();
    }

    private void applyStageEffects(EntityMaid maid, MaidPatch<?> patch, int stage) {
        removeStageEffects(maid);

        switch (stage) {
            case 1:
                maid.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 0, false, false));
                break;
            case 2:
                maid.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 0, false, false));
                maid.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, Integer.MAX_VALUE, 0, false, false));
                break;
            case 3:
                maid.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 0, false, false));
                maid.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, Integer.MAX_VALUE, 0, false, false));
                maid.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
                break;
            case 4:
                maid.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 1, false, false));
                maid.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, Integer.MAX_VALUE, 0, false, false));
                maid.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));
                maid.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 0, false, false));
                break;
        }
    }

    private void removeStageEffects(EntityMaid maid) {
        maid.removeEffect(MobEffects.DAMAGE_BOOST);
        maid.removeEffect(MobEffects.DIG_SPEED);
        maid.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        maid.removeEffect(MobEffects.MOVEMENT_SPEED);
    }

    private void spawnParticles(EntityMaid maid, ServerLevel level, int stage) {
        DustParticleOptions particle = switch (stage) {
            case 1 -> RED_PARTICLES;
            case 2 -> CYAN_PARTICLES;
            case 3 -> YELLOW_PARTICLES;
            case 4 -> PURPLE_PARTICLES;
            default -> null;
        };

        if (particle == null) return;

        Vec3 pos = maid.position();
        double x = pos.x;
        double y = pos.y + maid.getBbHeight() * 0.5;
        double z = pos.z;

        for (int i = 0; i < 3; i++) {
            double offsetX = (maid.getRandom().nextDouble() - 0.5) * 1.5;
            double offsetY = maid.getRandom().nextDouble() * 1.0;
            double offsetZ = (maid.getRandom().nextDouble() - 0.5) * 1.5;
            level.sendParticles(particle, x + offsetX, y + offsetY, z + offsetZ, 1, 0, 0, 0, 0);
        }
    }
}
