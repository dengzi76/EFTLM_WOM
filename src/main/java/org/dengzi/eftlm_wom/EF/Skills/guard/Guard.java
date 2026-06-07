package org.dengzi.eftlm_wom.EF.Skills.guard;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAttackEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Compat.EFNCompat;
import net.EFTLM.EF.Skill.MaidSkill;
import net.EFTLM.EF.Skill.MaidSkillBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

public class Guard extends MaidSkill {
    private static final float STAMINA_COST = 2.0f;
    private static final float PARRY_COST = 5.0f;
    private static final float COUNTER_COST = 3.0f;
    private static final float COUNTER_CHANCE = 0.3f;
    private static final float PARRY_WINDOW_TICKS = 6.0f;
    private static final float STAMINA_REGEN = 0.5f;
    private static final int GUARD_BREAK_STUN = 40;
    private static final int GUARD_COOLDOWN = 15;

    private static final String KEY_LAST_ACTION = "gd_lastAction";
    private static final String KEY_BREAK_TICK = "gd_breakTick";
    private static final String KEY_PARRY_INDEX = "gd_parryIdx";

    public Guard(MaidSkillBuilder builder) {
        super(builder);
    }

    @Override
    public void MaidTick(MaidTickEvent event) {
        EntityMaid maid = event.getMaid();
        if (maid.level().isClientSide()) return;
        MaidPatch patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
        if (patch == null) return;

        float stamina = patch.getStamina();
        if (stamina < patch.getMaxStamina()) {
            patch.setStamina(Math.min(patch.getMaxStamina(), stamina + STAMINA_REGEN));
        }

        CompoundTag data = maid.getPersistentData();
        if (data.contains(KEY_BREAK_TICK)) {
            int breakTick = data.getInt(KEY_BREAK_TICK);
            if (maid.tickCount - breakTick > GUARD_BREAK_STUN) {
                data.remove(KEY_BREAK_TICK);
            }
        }
    }

    @Override
    public void MaidAttack(MaidAttackEvent event) {
        EntityMaid maid = event.getMaid();
        MaidPatch patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
        DamageSource source = event.getSource();
        Level level = maid.level();

        if (!(level instanceof ServerLevel serverLevel)) return;
        if (patch == null) return;

        PlayerPatch<?> ownerPatch = patch.getOwnerPatch();
        Entity attacker = source.getEntity();
        if (ownerPatch != null && attacker != null && ownerPatch.getOriginal().equals(attacker)) return;
        if (EFNCompat.isInvulnerability(patch) || EFNCompat.isImmunity(patch)) return;

        CompoundTag data = maid.getPersistentData();
        if (data.contains(KEY_BREAK_TICK)) return;

        int stateLevel = patch.getEntityState().getLevel();
        if (stateLevel <= 0 || stateLevel >= 3 || !patch.canUseSkill()
            || !isFrontAttack(source, maid) || !isBlockableSource(source)) return;

        float stamina = patch.getStamina();
        int lastAction = data.getInt(KEY_LAST_ACTION);
        boolean inParryWindow = maid.tickCount - lastAction < PARRY_WINDOW_TICKS;
        float cost = inParryWindow ? STAMINA_COST + PARRY_COST : STAMINA_COST;

        if (!patch.hasStamina(cost)) {
            playGuardBreak(maid, patch, serverLevel, source, data);
            return;
        }

        patch.setStamina(stamina - cost);
        patch.setCoolDown(GUARD_COOLDOWN);

        ItemStack handStack = maid.getMainHandItem();
        CapabilityItem cap = patch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        MaidGuardData guardData = MaidGuardRegistry.get(handStack, cap);

        if (inParryWindow) {
            handleParry(event, maid, patch, serverLevel, source, attacker, handStack, guardData, data);
            return;
        }

        handleNormalBlock(event, maid, patch, serverLevel, source, guardData, data);
    }

    private void handleParry(MaidAttackEvent event, EntityMaid maid, MaidPatch patch, ServerLevel serverLevel,
                             DamageSource source, Entity attacker, ItemStack handStack, MaidGuardData guardData, CompoundTag data) {
        patch.playSound(EpicFightSounds.CLASH.get(), 0.0f, 0.0f);

        AnimationAccessor<? extends StaticAnimation> parryAnim = guardData.parry;
        if (parryAnim == null) {
            int idx = data.getInt(KEY_PARRY_INDEX);
            data.putInt(KEY_PARRY_INDEX, idx + 1);
            patch.playAnimationSynchronized(guardData.getHit(idx), 0.0F);
        } else {
            patch.playAnimationSynchronized(parryAnim, 0.0F);
        }

        spawnHitParticle(serverLevel, patch, maid, source);
        event.setCanceled(true);

        if (guardData.counter != null && attacker instanceof LivingEntity
            && maid.level().random.nextFloat() < guardData.counterChance
            && patch.hasStamina(guardData.counterCost)) {
            patch.setStamina(patch.getStamina() - guardData.counterCost);
            patch.playAnimationSynchronized(guardData.counter, 0.0F);
        }

        data.remove(KEY_LAST_ACTION);
    }

    private void handleNormalBlock(MaidAttackEvent event, EntityMaid maid, MaidPatch patch, ServerLevel serverLevel,
                                   DamageSource source, MaidGuardData guardData, CompoundTag data) {
        patch.playSound(EpicFightSounds.CLASH.get(), -0.05f, 0.1f);

        int idx = data.getInt(KEY_PARRY_INDEX);
        data.putInt(KEY_PARRY_INDEX, idx + 1);
        patch.playAnimationSynchronized(guardData.getHit(idx), 0.0F);

        spawnHitParticle(serverLevel, patch, maid, source);
        event.setCanceled(true);

        data.putInt(KEY_LAST_ACTION, maid.tickCount);
    }

    private void playGuardBreak(EntityMaid maid, MaidPatch patch, ServerLevel serverLevel, DamageSource source, CompoundTag data) {
        patch.playSound(EpicFightSounds.NEUTRALIZE_MOBS.get(), -0.05f, 0.1f);
        patch.playAnimationSynchronized(Animations.BIPED_HIT_LONG, 0.0F);
        patch.setCoolDown(GUARD_BREAK_STUN);
        data.putInt(KEY_BREAK_TICK, maid.tickCount);
        spawnHitParticle(serverLevel, patch, maid, source);
    }

    private void spawnHitParticle(ServerLevel serverLevel, MaidPatch patch, Entity maid, DamageSource source) {
        EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(
            serverLevel, HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, maid, source.getDirectEntity());
    }

    private boolean isFrontAttack(DamageSource source, Entity maid) {
        if (source.getEntity() == null) return false;
        Vec3 toAttacker = source.getEntity().position().subtract(maid.position()).normalize();
        Vec3 lookVec = maid.getLookAngle();
        return toAttacker.dot(lookVec) > 0;
    }

    private boolean isBlockableSource(DamageSource source) {
        return !source.is(DamageTypeTags.IS_FALL)
            && !source.is(DamageTypeTags.IS_DROWNING)
            && !source.is(DamageTypeTags.IS_EXPLOSION)
            && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)
            && !source.is(EpicFightDamageTypeTags.UNBLOCKALBE)
            && !source.is(DamageTypes.FELL_OUT_OF_WORLD)
            && !source.is(DamageTypes.STARVE);
    }
}
