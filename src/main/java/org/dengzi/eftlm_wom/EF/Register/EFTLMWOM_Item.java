package org.dengzi.eftlm_wom.EF.Register;

import net.EFTLM.EF.Item.MaidSkillBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EFTLMWOM_Item {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "eftlm_wom");

    public static final RegistryObject<Item> SKILLBOOK =
            ITEMS.register("skillbook", () -> new MaidSkillBookItem((new Item.Properties()).rarity(Rarity.RARE).stacksTo(1)));
}
