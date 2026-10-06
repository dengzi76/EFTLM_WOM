package org.dengzi76.eftlm_wom.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationParameters;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/** WOM 2.0.171 has unconditional Player casts in these callbacks; maid equivalents are native. */
@Mixin(value = AnimationEvent.class, remap = false)
public abstract class WomAnimationEventMixin {
    @Shadow @Final protected AnimationEvent.Side side;

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private void eftlmWom$skipPlayerOnlyCallback(LivingEntityPatch<?> patch,
            AssetAccessor<? extends StaticAnimation> animation, float previous, float elapsed,
            CallbackInfo ci) {
        eftlmWom$checkCallback(patch, animation, previous, elapsed, ci);
    }

    @Inject(method = "executeWithNewParams", at = @At("HEAD"), cancellable = true)
    private void eftlmWom$skipPlayerOnlyCallbackWithParams(LivingEntityPatch<?> patch,
            AssetAccessor<? extends StaticAnimation> animation, float previous, float elapsed,
            AnimationParameters parameters, CallbackInfo ci) {
        eftlmWom$checkCallback(patch, animation, previous, elapsed, ci);
    }

    @org.spongepowered.asm.mixin.Unique
    private void eftlmWom$checkCallback(LivingEntityPatch<?> patch,
            AssetAccessor<? extends StaticAnimation> animation, float previous, float elapsed, CallbackInfo ci) {
        if (!(patch.getOriginal() instanceof EntityMaid) || animation.registryName() == null
                || !animation.registryName().getNamespace().equals("wom")) return;
        String path = animation.registryName().getPath();
        if (path.equals("biped/skill/ruine_plunder") || side == AnimationEvent.Side.CLIENT
                && (path.equals("biped/skill/moonless_lunar_echo") || path.equals("biped/skill/moonless_lunar_eclipse"))) ci.cancel();
    }
}
