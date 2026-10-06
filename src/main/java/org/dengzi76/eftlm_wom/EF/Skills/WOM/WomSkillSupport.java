package org.dengzi76.eftlm_wom.EF.Skills.WOM;

import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkillManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public final class WomSkillSupport {
    private WomSkillSupport() { }

    public static boolean learned(MaidPatch<?> patch, String id) {
        return patch.hasLearnedSkill(ResourceLocation.fromNamespaceAndPath("eftlm_wom", id));
    }

    public static boolean ready(MaidPatch<?> patch) {
        return !patch.isLogicalClient() && patch.isFightMode() && !patch.CheckState()
                && patch.getOriginal().getPersistentData().getInt("eftlm_wom:action_tick") != patch.getOriginal().tickCount
                && patch.getEntityState().canUseSkill() && patch.getEntityState().canBasicAttack()
                && !patch.getEntityState().inaction();
    }

    public static boolean enemy(MaidPatch<?> patch, LivingEntity entity) {
        var maid = patch.getOriginal();
        if (!entity.isAlive() || entity == maid || entity == maid.getOwner() || maid.isAlliedTo(entity)) return false;
        if (entity instanceof OwnableEntity owned && maid.getOwnerUUID() != null
                && maid.getOwnerUUID().equals(owned.getOwnerUUID())) return false;
        return entity == patch.getTarget() || entity instanceof net.minecraft.world.entity.monster.Enemy
                || entity instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == maid;
    }

    public static boolean threatened(MaidPatch<?> patch) {
        LivingEntity target = patch.getTarget();
        if (target == null || !target.isAlive() || patch.getOriginal().distanceToSqr(target) > 36) return false;
        var targetPatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
        // Vanilla AI can attack without entering an Epic Fight animation state.
        if (target instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == patch.getOriginal()) return true;
        if (targetPatch != null && targetPatch.getEntityState().getLevel() > 0
                && targetPatch.getEntityState().getLevel() < 3) return true;
        // Anticipate melee attacks from a visible hostile target within 3.5 blocks.
        return target instanceof net.minecraft.world.entity.monster.Enemy
                && patch.getOriginal().distanceToSqr(target) <= 12.25
                && patch.getOriginal().hasLineOfSight(target);
    }

    public static boolean consume(MaidPatch<?> patch, float amount) {
        if (learned(patch, "lunatic_vivacity") && patch.getTarget() != null) {
            var skill = MaidSkillManager.getSkillFor(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "lunatic_vivacity"));
            Integer timer = patch.getDataValue(skill, WomMaidSkill.TIMER);
            amount *= timer != null && timer > 200 ? 0.15F : 1.85F;
        }
        if (!patch.hasStamina(amount)) {
            if (!learned(patch, "voodoo_magic") || patch.getOriginal().getHealth() <= 0.5F) return false;
            patch.getOriginal().setHealth(Math.max(0.5F, patch.getOriginal().getHealth() - 0.5F));
            patch.setStamina(Math.min(patch.getMaxStamina(), patch.getStamina() + amount * 0.8F));
            if (!patch.hasStamina(amount)) return false;
        }
        patch.setStamina(Math.max(0, patch.getStamina() - amount));
        return true;
    }

    public static void play(MaidPatch<?> patch, AnimationAccessor<? extends StaticAnimation> animation) {
        patch.getOriginal().getPersistentData().putInt("eftlm_wom:action_tick", patch.getOriginal().tickCount);
        patch.getOriginal().getNavigation().stop();
        if (patch.getTarget() != null) patch.getOriginal().lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,
                patch.getTarget().getEyePosition());
        patch.playAnimationSynchronized(animation, 0F);
    }

    public static void defenseSuccess(MaidPatch<?> patch, boolean parry) {
        for (var id : patch.getLearnedSkills()) {
            var skill = MaidSkillManager.getSkillFor(id);
            if (skill instanceof WomMaidSkill wom && wom.canExecute(patch)) wom.onDefenseSuccess(patch, parry);
        }
    }

    public static boolean safeTeleport(MaidPatch<?> patch, Vec3 pos) {
        var maid = patch.getOriginal();
        var destination = maid.getBoundingBox().move(pos.subtract(maid.position()));
        if (!maid.level().hasChunkAt(net.minecraft.core.BlockPos.containing(pos))
                || !maid.level().getWorldBorder().isWithinBounds(destination)
                || !maid.level().noCollision(maid, destination)) return false;
        maid.teleportTo(pos.x, pos.y, pos.z);
        return true;
    }
}
