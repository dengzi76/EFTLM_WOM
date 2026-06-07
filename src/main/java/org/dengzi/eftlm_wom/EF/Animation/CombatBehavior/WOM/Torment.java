package org.dengzi.eftlm_wom.EF.Animation.CombatBehavior.WOM;

import net.EFTLM.EF.Animation.CombatBehavior.BehaviorsBuild;
import org.dengzi.eftlm_wom.EF.Compat.CompatModList;
import reascer.wom.gameasset.WOMAnimations;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class Torment {
    public static CombatBehaviors.Builder<HumanoidMobPatch<?>> Instance;

    public Torment() {
    }

    static {
        if (CompatModList.LoadedWOM()) {
            Instance = CombatBehaviors.<HumanoidMobPatch<?>>builder().newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_AUTO_1).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_AUTO_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_CHARGED_ATTACK_1).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_AUTO_4).withinDistance(0.0, 5.0)))
                    .newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_DASH).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_CHARGED_ATTACK_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_AUTO_3).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_CHARGED_ATTACK_1).withinDistance(0.0, 5.0)))
                    .newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().custom(BehaviorsBuild::canUseSkill).behavior((Patch) -> {Patch.playAnimationSynchronized(WOMAnimations.TORMENT_BERSERK_AIRSLAM, 0.0F);BehaviorsBuild.setCoolDown(Patch, 0);}).withinDistance(0.0, 24.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_BERSERK_AUTO_1).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_BERSERK_AUTO_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_BERSERK_CONVERT).withinDistance(0.0, 5.0)))
                    .newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_BERSERK_DASH).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_DASH).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_AIRSLAM).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.TORMENT_AUTO_4).withinDistance(0.0, 5.0)));

        }
    }

}
