package org.dengzi.eftlm_wom.EF.Compat;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.item.Item;
import org.dengzi.eftlm_wom.EF.Animation.CombatBehavior.WOM.*;
import reascer.wom.world.item.WOMItems;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.Map;

public class WOMCompat {
    public static void trySetWeaponMotions(Map<Item, CombatBehaviors.Builder<HumanoidMobPatch<?>>> ItemAttackMotions, Map<Item, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> SpecialItemAttackMotions, Map<Item, HumanoidArmature> ItemArmatures) {
        if (CompatModList.LoadedWOM()) {
            WOMCompat.Internal.setupWeaponMotions(ItemAttackMotions, SpecialItemAttackMotions, ItemArmatures);
        }

    }
    protected static class Internal {
        protected Internal() {
        }
        static void setupWeaponMotions(Map<Item, CombatBehaviors.Builder<HumanoidMobPatch<?>>> ItemAttackMotions, Map<Item, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> SpecialItemAttackMotions, Map<Item, HumanoidArmature> ItemArmatures){
            ItemAttackMotions.put(WOMItems.MOONLESS.get(), Moonless.Instance);
            ItemAttackMotions.put(WOMItems.RUINE.get(), Ruine.Instance);
            SpecialItemAttackMotions.put(WOMItems.ENDER_BLASTER.get(), ImmutableMap.of(Styles.ONE_HAND, Ender_Blaster_OneHand.Instance, Styles.TWO_HAND, Ender_Blaster_TwoHand.Instance));
            ItemAttackMotions.put(WOMItems.WOODEN_STAFF.get(), Staff.Instance);
            ItemAttackMotions.put(WOMItems.STONE_STAFF.get(), Staff.Instance);
            ItemAttackMotions.put(WOMItems.IRON_STAFF.get(), Staff.Instance);
            ItemAttackMotions.put(WOMItems.GOLDEN_STAFF.get(), Staff.Instance);
            ItemAttackMotions.put(WOMItems.DIAMOND_STAFF.get(), Staff.Instance);
            ItemAttackMotions.put(WOMItems.NETHERITE_STAFF.get(), Staff.Instance);
            ItemAttackMotions.put(WOMItems.HERRSCHER.get(), Herrscher.Instance);
            ItemAttackMotions.put(WOMItems.SOLAR.get(), Solar.Instance);
            ItemAttackMotions.put(WOMItems.AGONY.get(), Agony.Instance);
            ItemAttackMotions.put(WOMItems.TORMENTED_MIND.get(), Torment.Instance);
            ItemAttackMotions.put(WOMItems.SATSUJIN.get(), Satsujin.Instance);
            ItemAttackMotions.put(WOMItems.ANTITHEUS.get(), Antitheus.Instance);
            ItemAttackMotions.put(WOMItems.NAPOLEON.get(), Napoleon.Instance);
            ItemAttackMotions.put(WOMItems.BLACKSTAR.get(), Blackstar.Instance);
            ItemAttackMotions.put(WOMItems.ORBIT.get(), Orbit.Instance);
            SpecialItemAttackMotions.put(WOMItems.NOVA.get(), ImmutableMap.of(Styles.ONE_HAND, Nova_OneHand.Instance, Styles.TWO_HAND, Nova_TwoHand.Instance));
            ItemAttackMotions.put(WOMItems.EVIL_TACHI.get(), Evil_Tachi.Instance);
            ItemAttackMotions.put(WOMItems.CELERITY_BRACELET.get(), Clawed_Gauntle.Instance);
        }
    }
}
