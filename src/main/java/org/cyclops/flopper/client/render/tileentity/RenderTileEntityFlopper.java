package org.cyclops.flopper.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
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
                Triple<Float, Float, Float> color = Helpers.intToRGB(fluid.getFluid().getAttributes().getColor());

                IVertexBuilder vb = buffer.getBuffer(RenderType.getText(icon.getAtlasTexture().getTextureLocation()));
                Matrix4f matrix = matrixStack.getLast().getMatrix();
                vb.pos(matrix, 0.125F, height, 0.125F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMinU(), icon.getMaxV()).lightmap(l2, i3).endVertex();
                vb.pos(matrix, 0.125F, height, 0.875F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMinU(), icon.getMinV()).lightmap(l2, i3).endVertex();
                vb.pos(matrix, 0.875F, height, 0.875F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMaxU(), icon.getMinV()).lightmap(l2, i3).endVertex();
                vb.pos(matrix, 0.875F, height, 0.125F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMaxU(), icon.getMaxV()).lightmap(l2, i3).endVertex();
            });
        }
    }

}
