package org.dengzi.eftlm_wom.EF.Animation.CombatBehavior.WOM;

import net.EFTLM.EF.Animation.CombatBehavior.BehaviorsBuild;
import org.dengzi.eftlm_wom.EF.Compat.CompatModList;
import reascer.wom.gameasset.animations.weapons.AnimsEnderblaster;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class Clawed_Gauntle {
    public static CombatBehaviors.Builder<HumanoidMobPatch<?>> Instance;

    static {
        if (CompatModList.LoadedWOM()) {
            Instance = CombatBehaviors.<HumanoidMobPatch<?>>builder().newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_AUTO_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_AUTO_3).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.FIST_AUTO2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_DASH).withinDistance(0.0, 5.0)))
                    .newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_AUTO_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_AUTO_3).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.FIST_AUTO2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_TWOHAND_TISHNAW).withinDistance(0.0, 5.0)))
                                .newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(500).weight(20.0F).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().custom(BehaviorsBuild::canUseSkill).behavior((Patch) -> {
                Patch.playAnimationSynchronized(Animations.RELENTLESS_COMBO, 0.0F);
                BehaviorsBuild.setCoolDown(Patch, 120);
            }).withinDistance(0.0, 24.0)));
        }
    }
}
