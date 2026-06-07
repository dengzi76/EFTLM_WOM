package org.dengzi.eftlm_wom.EF.Event;

import net.EFTLM.EF.API.Event.MaidSkillBuildEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.dengzi.eftlm_wom.EF.Skills.Passive.Meditation;

@EventBusSubscriber(
        modid = "eftlm_wom",
        bus = Bus.MOD
)
public class SkillEventBus {
    public SkillEventBus() {
    }

    @SubscribeEvent
    public static void MaidSkillBuild(MaidSkillBuildEvent event) {
        event.build(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "meditation"), Meditation::new, Meditation.createBuilder());
    }
}
