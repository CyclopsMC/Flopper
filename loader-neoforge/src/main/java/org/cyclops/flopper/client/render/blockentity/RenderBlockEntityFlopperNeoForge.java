package org.cyclops.flopper.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.IRenderHelpersNeoForge;
import org.cyclops.flopper.Flopper;
import org.cyclops.flopper.blockentity.BlockEntityFlopperNeoForge;
import org.joml.Matrix4f;

/**
 * Renderer for the item inside the {@link org.cyclops.flopper.block.BlockFlopper}.
 *
 * @author rubensworks
 *
 */
public class RenderBlockEntityFlopperNeoForge implements BlockEntityRenderer<BlockEntityFlopperNeoForge> {

    public RenderBlockEntityFlopperNeoForge(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(BlockEntityFlopperNeoForge tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if(tile != null) {
            FluidStack fluid = tile.getTank().getFluid();
            IRenderHelpersNeoForge renderHelpers = Flopper._instance.getModHelpers().getRenderHelpers();
            renderHelpers.renderFluidContext(fluid, matrixStack, () -> {
                float height = (fluid.getAmount() * 0.3125F) / tile.getTank().getCapacity() + 0.6875F;
                int brightness = Math.max(combinedLight, fluid.getFluid().getFluidType().getLightLevel(fluid));
                int l2 = brightness >> 0x10 & 0xFFFF;
                int i3 = brightness & 0xFFFF;

                TextureAtlasSprite icon = renderHelpers.getFluidIcon(tile.getTank().getFluid(), Direction.UP);
                IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid.getFluid());
                Triple<Float, Float, Float> color = Flopper._instance.getModHelpers().getBaseHelpers().intToRGB(renderProperties.getTintColor(fluid.getFluid().defaultFluidState(), tile.getLevel(), tile.getBlockPos()));

                VertexConsumer vb = buffer.getBuffer(RenderType.text(icon.atlasLocation()));
                Matrix4f matrix = matrixStack.last().pose();
                vb.addVertex(matrix, 0.125F, height, 0.125F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU0(), icon.getV1()).setUv2(l2, i3);
                vb.addVertex(matrix, 0.125F, height, 0.875F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU0(), icon.getV0()).setUv2(l2, i3);
                vb.addVertex(matrix, 0.875F, height, 0.875F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU1(), icon.getV0()).setUv2(l2, i3);
                vb.addVertex(matrix, 0.875F, height, 0.125F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU1(), icon.getV1()).setUv2(l2, i3);
            });
        }
    }

}
