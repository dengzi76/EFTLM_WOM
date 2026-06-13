package org.dengzi.eftlm_wom.EF.Animation.CombatBehavior.WOM;

import org.dengzi.eftlm_wom.EF.Compat.CompatModList;
import reascer.wom.gameasset.WOMAnimations;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class Antitheus {

    public static CombatBehaviors.Builder<HumanoidMobPatch<?>> Instance;

    public Antitheus() {
    }

    static {
        if (CompatModList.LoadedWOM()) {
            Instance = CombatBehaviors.<HumanoidMobPatch<?>>builder().newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.ANTITHEUS_AUTO_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.ANTITHEUS_AUTO_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.ANTITHEUS_AUTO_3).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(WOMAnimations.ANTITHEUS_AUTO_4).withinDistance(0.0, 5.0)));
        }
    }
}
