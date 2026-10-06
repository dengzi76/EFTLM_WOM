package org.dengzi76.eftlm_wom.EF.Event;

import net.EFTLM.EF.API.Event.MaidSkillBuildEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.dengzi76.eftlm_wom.EF.Register.EFTLMWOM_TAB;
import org.dengzi76.eftlm_wom.EF.Skills.Guard;
import org.dengzi76.eftlm_wom.EF.Skills.WOM.WomSkillCatalog;

@EventBusSubscriber(
        modid = "eftlm_wom",
        bus = Bus.MOD
)
public class SkillEventBus {
    public SkillEventBus() {
    }

    @SubscribeEvent
    public static void MaidSkillBuild(MaidSkillBuildEvent event) {
        WomSkillCatalog.register(event);
        event.build(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "guard"), Guard::new, Guard.createBuilder()
                .setCreativeTab(EFTLMWOM_TAB.Skill.get())
                .setIcon(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "textures/item/skillbook.png")));
    }
}
