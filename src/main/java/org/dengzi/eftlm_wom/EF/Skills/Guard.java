package org.dengzi.eftlm_wom.EF.Skills;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAttackEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Compat.EFNCompat;
import net.EFTLM.EF.Skill.MaidSkill;
import net.EFTLM.EF.Skill.MaidSkillBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import reascer.wom.gameasset.animations.weapons.AnimsAgony;
import reascer.wom.gameasset.animations.weapons.AnimsHerrscher;
import reascer.wom.gameasset.animations.weapons.AnimsMoonless;
import reascer.wom.gameasset.animations.weapons.AnimsRuine;
import reascer.wom.gameasset.animations.weapons.AnimsSatsujin;
import reascer.wom.gameasset.animations.weapons.AnimsSolar;
import reascer.wom.world.item.WOMItems;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

public class Guard extends MaidSkill {
    public Guard(MaidSkillBuilder builder) {
        super(builder);
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

        int stateLevel = patch.getEntityState().getLevel();
        if (stateLevel > 0 && stateLevel < 3 && patch.canUseSkill() && isFrontAttack(source, maid) && isBlockableSource(source)) {
            patch.playSound(EpicFightSounds.CLASH.get(), -0.05f, 0.1f);
            patch.setCoolDown(15);

            AnimationAccessor<? extends StaticAnimation> guardAnim = getGuardAnimation(maid.getMainHandItem(), patch);
            patch.playAnimationSynchronized(guardAnim, 0.0F);

            EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(serverLevel, HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, maid, source.getDirectEntity());
            event.setCanceled(true);
        }
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

    private AnimationAccessor<? extends StaticAnimation> getGuardAnimation(ItemStack stack, MaidPatch patch) {
        if (!stack.isEmpty()) {
            if (stack.getItem() == WOMItems.SOLAR.get()) return AnimsSolar.SOLAR_GUARD;
            if (stack.getItem() == WOMItems.MOONLESS.get()) return AnimsMoonless.MOONLESS_GUARD;
            if (stack.getItem() == WOMItems.RUINE.get()) return AnimsRuine.RUINE_GUARD;
            if (stack.getItem() == WOMItems.AGONY.get()) return AnimsAgony.AGONY_GUARD;
            if (stack.getItem() == WOMItems.HERRSCHER.get()) return AnimsHerrscher.HERRSCHER_GUARD;
            if (stack.getItem() == WOMItems.SATSUJIN.get()) return AnimsSatsujin.SATSUJIN_GUARD;

            CapabilityItem cap = patch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
            if (cap != null) {
                WeaponCategory cat = cap.getWeaponCategory();
                if (cat == WeaponCategories.SWORD) return Animations.SWORD_GUARD;
                if (cat == WeaponCategories.LONGSWORD) return Animations.LONGSWORD_GUARD;
                if (cat == WeaponCategories.GREATSWORD) return Animations.GREATSWORD_GUARD;
                if (cat == WeaponCategories.SPEAR) return Animations.SPEAR_GUARD;
                if (cat == WeaponCategories.TACHI) return Animations.UCHIGATANA_GUARD;
                if (cat == WeaponCategories.UCHIGATANA) return Animations.UCHIGATANA_GUARD;
                if (cat == WeaponCategories.DAGGER) return Animations.SWORD_GUARD;
                if (cat == WeaponCategories.AXE) return Animations.GREATSWORD_GUARD;
                if (cat == WeaponCategories.TRIDENT) return Animations.SPEAR_GUARD;
            }
        }

        return Animations.SWORD_GUARD;
    }
}
