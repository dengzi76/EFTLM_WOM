package org.dengzi.eftlm_wom;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.dengzi.eftlm_wom.EF.Skills.guard.MaidGuardRegistry;
import org.slf4j.Logger;

@Mod(eftlm_wom.MODID)
public class eftlm_wom {

    public static final String MODID = "eftlm_wom";
    private static final Logger LOGGER = LogUtils.getLogger();
    public eftlm_wom() {
        MaidGuardRegistry.init();
        LOGGER.info("EFTLM_WOM mod loaded!");
    }
}
