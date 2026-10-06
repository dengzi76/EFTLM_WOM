package org.dengzi76.eftlm_wom.EF.Event;

import java.util.UUID;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkillManager;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.Enchantments;
import reascer.wom.world.entity.mob.EnderHand;
import reascer.wom.gameasset.animations.weapons.AnimsRuine;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dengzi76.eftlm_wom.EF.Skills.WOM.WomMaidSkill;
import org.dengzi76.eftlm_wom.EF.Skills.WOM.WomSkillSupport;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mod.EventBusSubscriber(modid = "eftlm_wom")
public final class WomMaidSkillEvents {
    private static final UUID SPRINT_SPEED = UUID.fromString("aa11d073-10f3-4cfe-8a4c-ed72ea297085");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void damage(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.isCanceled()) return;
        var attackerEntity = event.getSource().getEntity();
        MaidPatch<?> attacker = attackerEntity == null ? null : EpicFightCapabilities.getEntityPatch(attackerEntity, MaidPatch.class);
        if (attacker != null && event.getSource() instanceof EpicFightDamageSource source
                && source.getAnimation().registryName() != null && source.getAnimation().registryName().getNamespace().equals("wom")
                && !WomSkillSupport.enemy(attacker, event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        if (attacker != null) for (var id : attacker.getLearnedSkills()) {
            var skill = MaidSkillManager.getSkillFor(id);
            if (skill instanceof WomMaidSkill wom && wom.canExecute(attacker)) wom.modifyOutgoing(event, attacker);
        }
        MaidPatch<?> defender = EpicFightCapabilities.getEntityPatch(event.getEntity(), MaidPatch.class);
        if (defender != null) for (var id : defender.getLearnedSkills()) {
            var skill = MaidSkillManager.getSkillFor(id);
            if (skill instanceof WomMaidSkill wom && wom.canExecute(defender)) wom.modifyIncoming(event, defender);
        }
    }

    @SubscribeEvent
    public static void enderHand(LivingEvent.LivingTickEvent event) {
        // WOM EnderHand's original damage callback requires a ServerPlayerPatch owner.
        if (!(event.getEntity() instanceof EnderHand hand) || hand.level().isClientSide() || hand.tickCount != 20
                || hand.getOwner() == null || hand.getTarget() == null || !hand.getTarget().isAlive()) return;
        MaidPatch<?> patch = EpicFightCapabilities.getEntityPatch(hand.getOwner(), MaidPatch.class);
        if (patch == null || !WomSkillSupport.enemy(patch, hand.getTarget())) return;
        var maid = patch.getOriginal();
        var target = hand.getTarget();
        var source = patch.getDamageSource(AnimsRuine.RUINE_PLUNDER, InteractionHand.MAIN_HAND);
        source.setStunType(StunType.HOLD);
        source.setBaseImpact(4);
        source.attachDamageModifier(yesman.epicfight.api.utils.math.ValueModifier.multiplier(2.6F));
        target.hurt(source, (float) maid.getAttributeValue(Attributes.ATTACK_DAMAGE));
        int sweeping = maid.getMainHandItem().getEnchantmentLevel(Enchantments.SWEEPING_EDGE);
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, (9 + 3 * sweeping) * 20, 0));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (9 + 3 * sweeping) * 20, 0));
        maid.heal(1 + sweeping);
        patch.setStamina(Math.min(patch.getMaxStamina(), patch.getStamina() + patch.getMaxStamina() * 0.05F));
    }

    @SubscribeEvent
    public static void movement(MaidTickEvent event) {
        var maid = event.getMaid();
        if (maid.level().isClientSide()) return;
        var patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
        if (patch == null) return;
        var speed = maid.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        boolean sprint = patch.isFightMode() && !patch.CheckState() && WomSkillSupport.learned(patch, "natural_sprinter")
                && patch.getTarget() != null && maid.distanceToSqr(patch.getTarget()) > 4;
        if (sprint && speed.getModifier(SPRINT_SPEED) == null)
            speed.addTransientModifier(new AttributeModifier(SPRINT_SPEED, "WOM natural sprinter", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
        else if (!sprint) speed.removeModifier(SPRINT_SPEED);
    }
}
