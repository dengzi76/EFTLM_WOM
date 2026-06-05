package org.dengzi.eftlm_wom.EF.Event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.EFTLM.EF.Capability.MaidPatch;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dengzi.eftlm_wom.eftlm_wom;
import reascer.wom.world.item.WOMItems;
import yesman.epicfight.api.animation.ServerAnimator;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mod.EventBusSubscriber(modid = eftlm_wom.MODID)
public class SolarModelHandler {
    @SubscribeEvent
    public static void onMaidTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EntityMaid maid)) return;
        if (maid.level().isClientSide()) return;

        MaidPatch patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
        if (patch == null) return;

        ItemStack stack = maid.getMainHandItem();
        boolean hasTag = stack.hasTag() && stack.getTag().contains("obscuridad");

        if (stack.getItem() != WOMItems.SOLAR.get()) {
            if (hasTag) {
                stack.getTag().remove("obscuridad");
                maid.setItemSlot(EquipmentSlot.MAINHAND, stack);
            }
            return;
        }

        boolean isObscuridad = false;
        if (patch.getAnimator() instanceof ServerAnimator s) {
            var accessor = s.animationPlayer.getAnimation();
            isObscuridad = accessor != null && accessor.isPresent()
                && accessor.registryName() != null
                && accessor.registryName().getPath().contains("obscuridad");
        }

        if (isObscuridad != hasTag) {
            if (isObscuridad) {
                stack.getOrCreateTag().putInt("obscuridad", 1);
            } else {
                stack.getTag().remove("obscuridad");
            }
            maid.setItemSlot(EquipmentSlot.MAINHAND, stack);
        }
    }
}
