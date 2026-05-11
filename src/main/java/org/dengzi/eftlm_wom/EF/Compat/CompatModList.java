package org.dengzi.eftlm_wom.EF.Compat;

import net.minecraftforge.fml.ModList;

public class CompatModList {
    public static String WOM = "wom";


    public  CompatModList() {
    }

    public static boolean LoadedWOM() {
        return ModList.get().isLoaded(WOM);
    }
}
