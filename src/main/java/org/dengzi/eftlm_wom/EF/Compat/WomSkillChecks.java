package org.dengzi.eftlm_wom.EF.Compat;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.EFTLM.EF.Capability.MaidPatch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * WOM 兼容层：替代已从 EFTLM 1.3 移除的旧 BehaviorsBuild API
 * （canUseSkill / setCoolDown），实现方式不变：
 * 技能可用性 = 主手物品不在原版物品冷却中；释放后给主手物品加冷却模拟技能 CD。
 */
public final class WomSkillChecks {

    private WomSkillChecks() {
    }

    public static boolean LoadedWOM() {
        return ModList.get().isLoaded("wom");
    }

    /**
     * 技能可用判定（替代旧 BehaviorsBuild.canUseSkill）。
     */
    public static boolean canUseSkill(LivingEntityPatch<?> patch) {
        if (!(patch instanceof MaidPatch<?> maidPatch)) {
            return false;
        }
        EntityMaid maid = maidPatch.getOriginal();
        ItemStack stack = maid.getMainHandItem();
        return !stack.isEmpty() && !maid.getCooldowns().isOnCooldown(stack.getItem());
    }

    /**
     * 设置技能冷却（替代旧 BehaviorsBuild.setCoolDown）。
     */
    public static void setCoolDown(LivingEntityPatch<?> patch, int tick) {
        if (!(patch instanceof MaidPatch<?> maidPatch)) {
            return;
        }
        EntityMaid maid = maidPatch.getOriginal();
        if (maid.level() instanceof ServerLevel) {
            ItemStack stack = maid.getMainHandItem();
            if (!stack.isEmpty()) {
                maid.getCooldowns().addCooldown(stack.getItem(), tick);
            }
        }
    }
}
