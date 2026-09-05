package org.dengzi.eftlm_wom.EF.Register;

import net.EFTLM.EF.Item.MaidSkillBookItem;
import net.EFTLM.EF.Skill.MaidSkillManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class EFTLMWOM_TAB {
    public static final DeferredRegister<CreativeModeTab> Tab;
    public static final RegistryObject<CreativeModeTab> Skill;

    public EFTLMWOM_TAB() {}

    static {
        Tab = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "eftlm_wom");
        Skill = Tab.register("skills", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.eftlm_wom.skills"))
                .withTabsBefore(new ResourceKey[]{CreativeModeTabs.SPAWN_EGGS})
                .icon(() -> new ItemStack(EFTLMWOM_Item.SKILLBOOK.get()))
                .build());
    }
}
