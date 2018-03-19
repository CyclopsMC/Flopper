package org.cyclops.flopper.client.render.tileentity;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.flopper.tileentity.TileFlopper;
import org.lwjgl.opengl.GL11;

/**
 * Renderer for the item inside the {@link org.cyclops.flopper.block.BlockFlopper}.
 * 
 * @author rubensworks
 *
 */
public class RenderTileEntityFlopper extends TileEntitySpecialRenderer<TileFlopper> implements RenderHelpers.IFluidContextRender {

    private TileFlopper lastTile;

	@Override
	public void render(TileFlopper tile, double x, double y, double z, float partialTickTime, int partialDamage, float alpha) {
        if(tile != null) {
            lastTile = tile;
            RenderHelpers.renderTileFluidContext(tile.getTank().getFluid(), x, y, z, tile, this);
        }
	}

    @Override
    public void renderFluid(FluidStack fluid) {
        double height = (fluid.amount * 0.3125F) / lastTile.getTank().getCapacity() + 0.6875F;
        int brightness = lastTile.getWorld().getCombinedLight(lastTile.getPos(), fluid.getFluid().getLuminosity(fluid));
        int l2 = brightness >> 0x10 & 0xFFFF;
        int i3 = brightness & 0xFFFF;

        TextureAtlasSprite icon = RenderHelpers.getFluidIcon(lastTile.getTank().getFluid(), EnumFacing.UP);

        Tessellator t = Tessellator.getInstance();
        BufferBuilder worldRenderer = t.getBuffer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        worldRenderer.pos(0.125F, height, 0.125F).tex(icon.getMinU(), icon.getMaxV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
        worldRenderer.pos(0.125F, height, 0.875F).tex(icon.getMinU(), icon.getMinV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
        worldRenderer.pos(0.875F, height, 0.875F).tex(icon.getMaxU(), icon.getMinV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
        worldRenderer.pos(0.875F, height, 0.125F).tex(icon.getMaxU(), icon.getMaxV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();

        t.draw();
    }

}
