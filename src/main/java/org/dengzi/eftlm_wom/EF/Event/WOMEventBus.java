package org.dengzi.eftlm_wom.EF.Event;

import net.EFTLM.EF.API.Event.CombatBehaviorsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dengzi.eftlm_wom.EF.Compat.WOMCompat;
import org.dengzi.eftlm_wom.eftlm_wom;

@Mod.EventBusSubscriber(
        modid = eftlm_wom.MODID
)
public class WOMEventBus {
    @SubscribeEvent
    public static void MaidAttackBehaviors(CombatBehaviorsEvent event) {
        WOMCompat.trySetWeaponMotions(event.getItemAttackMotions(), event.getItemStyleAttackMotions(), event.getItemArmatures());
    }
}
