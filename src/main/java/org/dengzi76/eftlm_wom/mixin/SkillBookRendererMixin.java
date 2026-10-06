package org.dengzi76.eftlm_wom.mixin;

import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.EFTLM.EF.Item.MaidSkillBookItem;
import net.EFTLM.EF.Render.SkillBookRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Align both faces with the same texture silhouette so the back cannot leak through alpha. */
@Mixin(value = SkillBookRenderer.class, remap = false)
public abstract class SkillBookRendererMixin {
    @Inject(method = "renderByItem", remap = true, at = @At("HEAD"), cancellable = true)
    private void eftlmWom$renderGuiIcon(ItemStack stack, ItemDisplayContext context, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay, CallbackInfo callback) {
        if (context != ItemDisplayContext.GUI) return;
        var skill = MaidSkillBookItem.getContainSkill(stack);
        ResourceLocation icon = skill != null && skill.getItemIcon() != null
                ? skill.getItemIcon()
                : stack.getItem() == org.dengzi76.eftlm_wom.EF.Register.EFTLMWOM_Item.SKILLBOOK.get()
                        ? ResourceLocation.fromNamespaceAndPath("eftlm_wom", "textures/item/skillbook.png")
                        : SkillBookRenderer.DEFAULT_ICON;
        // Inventory icons need one plane, not a lit extrusion. Blending preserves the PNG's
        // antialiased alpha edges; the emissive shader avoids darkening it with entity lighting.
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityTranslucentEmissive(icon));
        PoseStack.Pose transform = pose.last();
        vertices.vertex(transform.pose(), 0, 0, 0.53125F).color(255, 255, 255, 255).uv(0, 1)
                .overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(transform.normal(), 0, 0, 1).endVertex();
        vertices.vertex(transform.pose(), 1, 0, 0.53125F).color(255, 255, 255, 255).uv(1, 1)
                .overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(transform.normal(), 0, 0, 1).endVertex();
        vertices.vertex(transform.pose(), 1, 1, 0.53125F).color(255, 255, 255, 255).uv(1, 0)
                .overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(transform.normal(), 0, 0, 1).endVertex();
        vertices.vertex(transform.pose(), 0, 1, 0.53125F).color(255, 255, 255, 255).uv(0, 0)
                .overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(transform.normal(), 0, 0, 1).endVertex();
        callback.cancel();
    }

    @Inject(method = "generateGeometry", at = @At("RETURN"))
    private static void eftlmWom$alignBackFace(ResourceLocation icon,
            CallbackInfoReturnable<List<float[]>> callback) {
        List<float[]> quads = callback.getReturnValue();
        if (quads.size() < 2) return;
        // EFTLM reverses the back-face vertex order but retains the front-face UV order.
        // Derive UVs from positions to retain winding and normals while aligning the alpha masks.
        float[] back = quads.get(1);
        for (int vertex = 0; vertex < 4; vertex++) {
            back[12 + vertex * 2] = back[vertex * 3];
            back[13 + vertex * 2] = 1.0F - back[vertex * 3 + 1];
        }
    }
}
