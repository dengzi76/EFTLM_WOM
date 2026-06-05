package org.dengzi.eftlm_wom.EF.Skills.Passive;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDamageEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidHurtEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkill;
import net.EFTLM.EF.Skill.MaidSkillBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import reascer.wom.gameasset.WOMAnimations;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public class Meditation extends MaidSkill {
    private static final String ACTIVE = "md_active";
    private static final String STAGE = "md_stage";
    private static final String TIMER = "md_timer";
    private static final String CYCLE = "md_cycle";
    private static final String IDLE = "md_idle";
    private static final String REGEN_APPLIED = "md_regen";

    private static final int STAGE_INTERVAL = 60;
    private static final int ANIM_CYCLE = 30;
    private static final int IDLE_THRESHOLD = 300;
    private static final int MAX_STAGE = 4;
    private static final float DAMAGE_BOOST = 1.4f;
    private static final float DAMAGE_RESIST = 0.5f;

    public Meditation(MaidSkillBuilder builder) {
        super(builder);
    }

    @Override
    public void MaidTick(MaidTickEvent event) {
        EntityMaid maid = event.getMaid();
        if (maid.level().isClientSide()) return;

        MaidPatch patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
        if (patch == null) return;

        CompoundTag data = maid.getPersistentData();
        boolean active = data.getBoolean(ACTIVE);
        int idleTicks = data.getInt(IDLE);

        if (!canBeIdle(maid, patch)) {
            if (active) stopMeditation(data, patch);
            data.putInt(IDLE, 0);
            return;
        }

        idleTicks++;
        data.putInt(IDLE, idleTicks);

        if (idleTicks < IDLE_THRESHOLD) {
            if (active) stopMeditation(data, patch);
            return;
        }

        if (!active) {
            startMeditation(data, patch);
            return;
        }

        tickAnimations(data, patch, maid);
        advanceStage(data, maid);
    }

    private boolean canBeIdle(EntityMaid maid, MaidPatch patch) {
        if (maid.getTarget() != null) return false;
        if (patch.isSleep()) return false;
        if (patch.isSit()) return false;
        if (maid.isInWater() || maid.isInLava()) return false;
        if (maid.isPassenger()) return false;
        return true;
    }

    private void startMeditation(CompoundTag data, MaidPatch patch) {
        data.putBoolean(ACTIVE, true);
        data.putInt(STAGE, 0);
        data.putInt(TIMER, STAGE_INTERVAL);
        data.putInt(CYCLE, 0);
        data.putBoolean(REGEN_APPLIED, false);
        patch.playAnimationSynchronized(WOMAnimations.MEDITATION_SITING, 0.0F);
    }

    private void stopMeditation(CompoundTag data, MaidPatch patch) {
        data.remove(ACTIVE);
        data.remove(STAGE);
        data.remove(TIMER);
        data.remove(CYCLE);
        data.remove(REGEN_APPLIED);
        patch.playAnimationSynchronized(Animations.BIPED_IDLE, 0.0F);
    }

    private void tickAnimations(CompoundTag data, MaidPatch patch, EntityMaid maid) {
        int cycle = data.getInt(CYCLE);
        cycle++;
        if (cycle >= ANIM_CYCLE) {
            cycle = 0;
            patch.playAnimationSynchronized(WOMAnimations.MEDITATION_BREATHING, 0.0F);
        }
        data.putInt(CYCLE, cycle);
    }

    private void advanceStage(CompoundTag data, EntityMaid maid) {
        int stage = data.getInt(STAGE);
        int timer = data.getInt(TIMER);

        if (stage < MAX_STAGE) {
            timer--;
            if (timer <= 0) {
                stage++;
                timer = stage < MAX_STAGE ? STAGE_INTERVAL : 0;
            }
        }

        if (stage >= MAX_STAGE) {
            boolean regenApplied = data.getBoolean(REGEN_APPLIED);
            if (!regenApplied) {
                maid.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, false, false));
                data.putBoolean(REGEN_APPLIED, true);
            }
        }

        data.putInt(STAGE, stage);
        data.putInt(TIMER, timer);
    }

    @Override
    public void MaidDamage(MaidDamageEvent event) {
        if (getStage(event.getMaid()) >= 1) {
            event.setAmount(event.getAmount() * DAMAGE_BOOST);
        }
    }

    @Override
    public void MaidHurt(MaidHurtEvent event) {
        if (getStage(event.getMaid()) >= 3) {
            event.setAmount(event.getAmount() * DAMAGE_RESIST);
        }
    }

    private int getStage(EntityMaid maid) {
        CompoundTag data = maid.getPersistentData();
        if (!data.getBoolean(ACTIVE)) return 0;
        return data.getInt(STAGE);
    }
}
