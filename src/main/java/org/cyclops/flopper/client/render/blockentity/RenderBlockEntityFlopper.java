package org.cyclops.flopper.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.flopper.blockentity.BlockEntityFlopper;
import org.joml.Matrix4f;

/**
 * Renderer for the item inside the {@link org.cyclops.flopper.block.BlockFlopper}.
 *
 * @author rubensworks
 *
 */
public class RenderBlockEntityFlopper implements BlockEntityRenderer<BlockEntityFlopper> {

    public RenderBlockEntityFlopper(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(BlockEntityFlopper tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if(tile != null) {
            FluidStack fluid = tile.getTank().getFluid();
            RenderHelpers.renderFluidContext(fluid, matrixStack, () -> {
                float height = (fluid.getAmount() * 0.3125F) / tile.getTank().getCapacity() + 0.6875F;
                int brightness = Math.max(combinedLight, fluid.getFluid().getFluidType().getLightLevel(fluid));
                int l2 = brightness >> 0x10 & 0xFFFF;
                int i3 = brightness & 0xFFFF;

                TextureAtlasSprite icon = RenderHelpers.getFluidIcon(tile.getTank().getFluid(), Direction.UP);
                IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid.getFluid());
                Triple<Float, Float, Float> color = Helpers.intToRGB(renderProperties.getTintColor(fluid.getFluid().defaultFluidState(), tile.getLevel(), tile.getBlockPos()));

                VertexConsumer vb = buffer.getBuffer(RenderType.text(icon.atlasLocation()));
                Matrix4f matrix = matrixStack.last().pose();
                vb.vertex(matrix, 0.125F, height, 0.125F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU0(), icon.getV1()).uv2(l2, i3).endVertex();
                vb.vertex(matrix, 0.125F, height, 0.875F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU0(), icon.getV0()).uv2(l2, i3).endVertex();
                vb.vertex(matrix, 0.875F, height, 0.875F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU1(), icon.getV0()).uv2(l2, i3).endVertex();
                vb.vertex(matrix, 0.875F, height, 0.125F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU1(), icon.getV1()).uv2(l2, i3).endVertex();
            });
        }
    }

}
