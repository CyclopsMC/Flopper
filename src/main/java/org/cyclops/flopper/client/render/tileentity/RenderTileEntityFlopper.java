package org.cyclops.flopper.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.flopper.tileentity.TileFlopper;

/**
 * Renderer for the item inside the {@link org.cyclops.flopper.block.BlockFlopper}.
 * 
 * @author rubensworks
 *
 */
public class RenderTileEntityFlopper extends TileEntityRenderer<TileFlopper> {

    public RenderTileEntityFlopper(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(TileFlopper tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if(tile != null) {
            FluidStack fluid = tile.getTank().getFluid();
            RenderHelpers.renderFluidContext(fluid, matrixStack, () -> {
                float height = (fluid.getAmount() * 0.3125F) / tile.getTank().getCapacity() + 0.6875F;
                int brightness = Math.max(combinedLight, fluid.getFluid().getAttributes().getLuminosity(fluid));
                int l2 = brightness >> 0x10 & 0xFFFF;
                int i3 = brightness & 0xFFFF;

                TextureAtlasSprite icon = RenderHelpers.getFluidIcon(tile.getTank().getFluid(), Direction.UP);
                Triple<Float, Float, Float> color = Helpers.intToRGB(fluid.getFluid().getAttributes().getColor(tile.getLevel(), tile.getBlockPos()));

                IVertexBuilder vb = buffer.getBuffer(RenderType.text(icon.atlas().location()));
                Matrix4f matrix = matrixStack.last().pose();
                vb.vertex(matrix, 0.125F, height, 0.125F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU0(), icon.getV1()).uv2(l2, i3).endVertex();
                vb.vertex(matrix, 0.125F, height, 0.875F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU0(), icon.getV0()).uv2(l2, i3).endVertex();
                vb.vertex(matrix, 0.875F, height, 0.875F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU1(), icon.getV0()).uv2(l2, i3).endVertex();
                vb.vertex(matrix, 0.875F, height, 0.125F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU1(), icon.getV1()).uv2(l2, i3).endVertex();
            });
        }
    }

}
