package org.dengzi.eftlm_wom.EF.Animation.CombatBehavior.WOM;

import org.dengzi.eftlm_wom.EF.Compat.CompatModList;
import reascer.wom.gameasset.animations.weapons.AnimsEnderblaster;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class Ender_Blaster_OneHand {
    public static CombatBehaviors.Builder<HumanoidMobPatch<?>> Instance;

    public Ender_Blaster_OneHand() {
    }

    static {
        if (CompatModList.LoadedWOM()) {
            Instance = CombatBehaviors.<HumanoidMobPatch<?>>builder().newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_SHOOT_2_LEFT).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_SHOOT_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_AUTO_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_DASH).withinDistance(0.0, 5.0)))
                    .newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_SHOOT_2_RIGHT).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_AUTO_4).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsEnderblaster.ENDERBLASTER_ONEHAND_SHOOT_2_FORWARD).withinDistance(0.0, 5.0)));
        }
    }
}
