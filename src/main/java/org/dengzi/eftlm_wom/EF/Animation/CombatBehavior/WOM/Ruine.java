package org.dengzi.eftlm_wom.EF.Animation.CombatBehavior.WOM;

import net.EFTLM.EF.Animation.CombatBehavior.BehaviorsBuild;
import org.dengzi.eftlm_wom.EF.Compat.CompatModList;
import reascer.wom.gameasset.animations.weapons.AnimsRuine;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class Ruine {
    public static CombatBehaviors.Builder<HumanoidMobPatch<?>> Instance;

    public Ruine(){
    }
    static {
        if (CompatModList.LoadedWOM()) {
            Instance = CombatBehaviors.<HumanoidMobPatch<?>>builder().newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_AUTO_1).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_AUTO_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_EXPIATION).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_REDEMPTION).withinDistance(0.0, 5.0)))
            .newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_AUTO_3).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_REDEMPTION).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_AUTO_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_AUTO_3).withinDistance(0.0, 5.0)))
            .newBehaviorSeries(CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().cooldown(20).weight(100.0F).canBeInterrupted(false).looping(false).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_AUTO_1).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_EXPIATION_1).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_EXPIATION_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(AnimsRuine.RUINE_AUTO_2).withinDistance(0.0, 5.0)).nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().custom(BehaviorsBuild::canUseSkill).behavior((Patch) -> {
                Patch.playAnimationSynchronized(AnimsRuine.RUINE_PLUNDER, 0.0F);
                BehaviorsBuild.setCoolDown(Patch, 0);
            }).withinDistance(0.0, 24.0)));
        }
    }
}
