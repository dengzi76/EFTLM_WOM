package org.dengzi.eftlm_wom.EF.Animation.CombatBehavior.WOM;

import net.EFTLM.EF.Animation.CombatBehavior.BehaviorsBuild;
import org.dengzi.eftlm_wom.EF.Compat.CompatModList;
import reascer.wom.gameasset.animations.weapons.AnimsSolar;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class Solar {
    public static CombatBehaviors.Builder<HumanoidMobPatch<?>> Instance;

    public Solar() {
    }

    static {
        if (CompatModList.LoadedWOM()) {
            Instance = CombatBehaviors.<HumanoidMobPatch<?>>builder().newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(25).weight(50.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsSolar.SOLAR_AUTO_1)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsSolar.SOLAR_AUTO_2)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsSolar.SOLAR_BRASERO_CREMATORIO)))
                    .newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(500).weight(20.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().custom(BehaviorsBuild::canUseSkill).behavior((Patch) -> {Patch.playAnimationSynchronized(AnimsSolar.SOLAR_BRASERO_CREMATORIO, 0.0F);BehaviorsBuild.setCoolDown(Patch, 1200);}).withinDistance(0.0, 24.0)));
        }
    }
}
