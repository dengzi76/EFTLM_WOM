package org.dengzi.eftlm_wom;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.dengzi.eftlm_wom.EF.Register.EFTLMWOM_Item;
import org.dengzi.eftlm_wom.EF.Register.EFTLMWOM_TAB;
import org.slf4j.Logger;

@Mod(eftlm_wom.MODID)
public class eftlm_wom {

    public static final String MODID = "eftlm_wom";
    private static final Logger LOGGER = LogUtils.getLogger();
    public eftlm_wom() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EFTLMWOM_Item.ITEMS.register(modEventBus);
        EFTLMWOM_TAB.Tab.register(modEventBus);
        LOGGER.info("EFTLM_WOM mod loaded!");
    }
}
