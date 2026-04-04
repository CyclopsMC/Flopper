package org.cyclops.flopper.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.IRenderHelpersForge;
import org.cyclops.flopper.FlopperForge;
import org.cyclops.flopper.blockentity.BlockEntityFlopperForge;
import org.jetbrains.annotations.Nullable;

/**
 * Renderer for the item inside the {@link org.cyclops.flopper.block.BlockFlopper}.
 *
 * @author rubensworks
 *
 */
public class RenderBlockEntityFlopperForge implements BlockEntityRenderer<BlockEntityFlopperForge, RenderBlockEntityFlopperForge.RenderState> {

    public RenderBlockEntityFlopperForge(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(BlockEntityFlopperForge blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        if (blockEntity != null) {
            renderState.fluid = blockEntity.getTank().getFluid();
            renderState.capacity = blockEntity.getTank().getCapacity();
        }
    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        FluidStack fluid = renderState.fluid;
        IRenderHelpersForge renderHelpers = FlopperForge._instance.getModHelpers().getRenderHelpers();
        renderHelpers.renderFluidContext(fluid, poseStack, () -> {
            float height = (fluid.getAmount() * 0.3125F) / renderState.capacity + 0.6875F;
            int brightness = Math.max(renderState.lightCoords, fluid.getFluid().getFluidType().getLightLevel(fluid));
            int l2 = brightness >> 0x10 & 0xFFFF;
            int i3 = brightness & 0xFFFF;

            TextureAtlasSprite icon = renderHelpers.getFluidIcon(renderState.fluid, Direction.UP);
            Triple<Float, Float, Float> color = renderHelpers.getFluidVertexBufferColor(fluid);

            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(icon.atlasLocation()), (pose, vb) -> {
                vb.addVertex(pose, 0.125F, height, 0.125F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU0(), icon.getV1()).setUv2(l2, i3);
                vb.addVertex(pose, 0.125F, height, 0.875F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU0(), icon.getV0()).setUv2(l2, i3);
                vb.addVertex(pose, 0.875F, height, 0.875F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU1(), icon.getV0()).setUv2(l2, i3);
                vb.addVertex(pose, 0.875F, height, 0.125F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU1(), icon.getV1()).setUv2(l2, i3);
            });
        });
    }

    public static class RenderState extends BlockEntityRenderState {
        public FluidStack fluid = FluidStack.EMPTY;
        public int capacity = 0;
    }

}
