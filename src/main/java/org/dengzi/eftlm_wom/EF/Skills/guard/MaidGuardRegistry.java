package org.dengzi.eftlm_wom.EF.Skills.guard;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.dengzi.eftlm_wom.EF.Skills.guard.MaidGuardData;
import reascer.wom.gameasset.animations.weapons.AnimsAgony;
import reascer.wom.gameasset.animations.weapons.AnimsHerrscher;
import reascer.wom.gameasset.animations.weapons.AnimsMoonless;
import reascer.wom.gameasset.animations.weapons.AnimsRuine;
import reascer.wom.gameasset.animations.weapons.AnimsSatsujin;
import reascer.wom.gameasset.animations.weapons.AnimsSolar;
import reascer.wom.world.item.WOMItems;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaidGuardRegistry {
    private static final Map<Item, MaidGuardData> ITEM_DATA = new HashMap<>();
    private static final Map<WeaponCategory, MaidGuardData> CATEGORY_DATA = new HashMap<>();
    private static boolean initialized = false;

    public static void register(Item item, MaidGuardData data) {
        ITEM_DATA.put(item, data);
    }

    public static void register(WeaponCategory category, MaidGuardData data) {
        CATEGORY_DATA.put(category, data);
    }

    public static MaidGuardData get(ItemStack stack, CapabilityItem cap) {
        if (!stack.isEmpty()) {
            MaidGuardData data = ITEM_DATA.get(stack.getItem());
            if (data != null) return data;
        }
        if (cap != null) {
            MaidGuardData data = CATEGORY_DATA.get(cap.getWeaponCategory());
            if (data != null) return data;
        }
        return CATEGORY_DATA.get(null);
    }

    public static void init() {
        if (initialized) return;
        initialized = true;

        register(WOMItems.SOLAR.get(), MaidGuardData.builder(AnimsSolar.SOLAR_GUARD)
            .hits(List.of(AnimsSolar.SOLAR_GUARD_HIT))
            .parry(AnimsSolar.SOLAR_GUARD_PARRY)
            .counter(AnimsSolar.SOLAR_GUARD_COUNTER)
            .build());

        register(WOMItems.MOONLESS.get(), MaidGuardData.builder(AnimsMoonless.MOONLESS_GUARD)
            .hits(List.of(AnimsMoonless.MOONLESS_GUARD_HIT_1, AnimsMoonless.MOONLESS_GUARD_HIT_2, AnimsMoonless.MOONLESS_GUARD_HIT_3))
            .build());

        register(WOMItems.RUINE.get(), MaidGuardData.builder(AnimsRuine.RUINE_GUARD)
            .hits(List.of(AnimsRuine.RUINE_BLOCK_1, AnimsRuine.RUINE_BLOCK_2))
            .counter(AnimsRuine.RUINE_PUNITION)
            .build());

        register(WOMItems.AGONY.get(), MaidGuardData.builder(AnimsAgony.AGONY_GUARD)
            .hits(List.of(AnimsAgony.AGONY_GUARD_HIT_1, AnimsAgony.AGONY_GUARD_HIT_2))
            .counter(AnimsAgony.AGONY_COUNTER)
            .build());

        register(WOMItems.HERRSCHER.get(), MaidGuardData.builder(AnimsHerrscher.HERRSCHER_GUARD)
            .hits(List.of(AnimsHerrscher.HERRSCHER_GUARD_HIT))
            .parry(AnimsHerrscher.HERRSCHER_GUARD_PARRY)
            .build());

        register(WOMItems.SATSUJIN.get(), MaidGuardData.builder(AnimsSatsujin.SATSUJIN_GUARD)
            .hits(List.of(AnimsSatsujin.SATSUJIN_GUARD_HIT))
            .build());

        register(WeaponCategories.SWORD, MaidGuardData.builder(Animations.SWORD_GUARD)
            .hits(List.of(Animations.SWORD_GUARD_HIT))
            .build());
        register(WeaponCategories.LONGSWORD, MaidGuardData.builder(Animations.LONGSWORD_GUARD)
            .hits(List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1))
            .build());
        register(WeaponCategories.GREATSWORD, MaidGuardData.builder(Animations.GREATSWORD_GUARD)
            .hits(List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1))
            .build());
        register(WeaponCategories.SPEAR, MaidGuardData.builder(Animations.SPEAR_GUARD)
            .hits(List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1))
            .build());
        register(WeaponCategories.DAGGER, MaidGuardData.builder(Animations.SWORD_GUARD)
            .hits(List.of(Animations.SWORD_GUARD_HIT))
            .build());
        register(WeaponCategories.TACHI, MaidGuardData.builder(Animations.UCHIGATANA_GUARD)
            .hits(List.of(Animations.SWORD_GUARD_HIT))
            .build());
        register(WeaponCategories.UCHIGATANA, MaidGuardData.builder(Animations.UCHIGATANA_GUARD)
            .hits(List.of(Animations.SWORD_GUARD_HIT))
            .build());
        register(WeaponCategories.AXE, MaidGuardData.builder(Animations.GREATSWORD_GUARD)
            .hits(List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1))
            .build());
        register(WeaponCategories.TRIDENT, MaidGuardData.builder(Animations.SPEAR_GUARD)
            .hits(List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1))
            .build());

        MaidGuardData fallback = MaidGuardData.builder(Animations.SWORD_GUARD)
            .hits(List.of(Animations.SWORD_GUARD_HIT))
            .build();
        CATEGORY_DATA.put(null, fallback);
    }
}
