package org.cyclops.flopper.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.IHopper;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.cyclops.cyclopscore.block.BlockTile;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.flopper.tileentity.TileFlopper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock.Properties;

/**
 * Fluid hopper block.
 * @author rubensworks
 */
public class BlockFlopper extends BlockTile {

    public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    // Copied from HopperBlock, to avoid conflicts with other mods messing with the hopper
    private static final VoxelShape INPUT_SHAPE = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape MIDDLE_SHAPE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
    private static final VoxelShape INPUT_MIDDLE_SHAPE = VoxelShapes.or(MIDDLE_SHAPE, INPUT_SHAPE);
    private static final VoxelShape BASE_SHAPE = VoxelShapes.join(INPUT_MIDDLE_SHAPE, IHopper.INSIDE, IBooleanFunction.ONLY_FIRST);
    private static final VoxelShape DOWN_SHAPE = VoxelShapes.or(BASE_SHAPE, Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));
    private static final VoxelShape EAST_SHAPE = VoxelShapes.or(BASE_SHAPE, Block.box(12.0D, 4.0D, 6.0D, 16.0D, 8.0D, 10.0D));
    private static final VoxelShape NORTH_SHAPE = VoxelShapes.or(BASE_SHAPE, Block.box(6.0D, 4.0D, 0.0D, 10.0D, 8.0D, 4.0D));
    private static final VoxelShape SOUTH_SHAPE = VoxelShapes.or(BASE_SHAPE, Block.box(6.0D, 4.0D, 12.0D, 10.0D, 8.0D, 16.0D));
    private static final VoxelShape WEST_SHAPE = VoxelShapes.or(BASE_SHAPE, Block.box(0.0D, 4.0D, 6.0D, 4.0D, 8.0D, 10.0D));
    private static final VoxelShape DOWN_RAYTRACE_SHAPE = IHopper.INSIDE;
    private static final VoxelShape EAST_RAYTRACE_SHAPE = VoxelShapes.or(IHopper.INSIDE, Block.box(12.0D, 8.0D, 6.0D, 16.0D, 10.0D, 10.0D));
    private static final VoxelShape NORTH_RAYTRACE_SHAPE = VoxelShapes.or(IHopper.INSIDE, Block.box(6.0D, 8.0D, 0.0D, 10.0D, 10.0D, 4.0D));
    private static final VoxelShape SOUTH_RAYTRACE_SHAPE = VoxelShapes.or(IHopper.INSIDE, Block.box(6.0D, 8.0D, 12.0D, 10.0D, 10.0D, 16.0D));
    private static final VoxelShape WEST_RAYTRACE_SHAPE = VoxelShapes.or(IHopper.INSIDE, Block.box(0.0D, 8.0D, 6.0D, 4.0D, 10.0D, 10.0D));

    public BlockFlopper(Properties properties, Supplier<CyclopsTileEntity> tileEntitySupplier) {
        super(properties, tileEntitySupplier);
        MinecraftForge.EVENT_BUS.register(this);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.DOWN)
                .setValue(ENABLED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        // Copied from HopperBlock, to avoid conflicts with other mods messing with the hopper
        switch((Direction)state.getValue(FACING)) {
            case DOWN:
                return DOWN_SHAPE;
            case NORTH:
                return NORTH_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case WEST:
                return WEST_SHAPE;
            case EAST:
                return EAST_SHAPE;
            default:
                return BASE_SHAPE;
        }
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        // Copied from HopperBlock, to avoid conflicts with other mods messing with the hopper
        switch((Direction)state.getValue(FACING)) {
            case DOWN:
                return DOWN_RAYTRACE_SHAPE;
            case NORTH:
                return NORTH_RAYTRACE_SHAPE;
            case SOUTH:
                return SOUTH_RAYTRACE_SHAPE;
            case WEST:
                return WEST_RAYTRACE_SHAPE;
            case EAST:
                return EAST_RAYTRACE_SHAPE;
            default:
                return IHopper.INSIDE;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getClickedFace().getOpposite();
        if (direction == Direction.UP) {
            direction = Direction.DOWN;
        }
        return this.defaultBlockState()
                .setValue(FACING, direction)
                .setValue(ENABLED, true);
    }

    @Override
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock()) {
            this.updateState(worldIn, pos, state);
        }
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        this.updateState(worldIn, pos, state);
    }

    private void updateState(World worldIn, BlockPos pos, BlockState state) {
        boolean notPowered = !worldIn.hasNeighborSignal(pos);
        if (notPowered != state.getValue(ENABLED)) {
            worldIn.setBlock(pos, state.setValue(ENABLED, notPowered), 4);
        }
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        ActionResultType activatedSuper = super.use(blockState, world, blockPos, player, hand, rayTraceResult);
        if (activatedSuper.consumesAction()) {
            return activatedSuper;
        }
        return TileHelpers.getCapability(world, blockPos, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                .map(fluidHandler -> {
                    ItemStack itemStack = player.getItemInHand(hand);
                    if (itemStack.isEmpty()) {
                        if (BlockFlopperConfig.showContentsStatusMessageOnClick) {
                            // If the hand is empty, show the tank contents
                            FluidStack fluidStack = fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                            if (fluidStack.isEmpty()) {
                                player.displayClientMessage(new StringTextComponent("0 / "
                                        + String.format("%,d", fluidHandler.getTankCapacity(0))), true);
                            } else {
                                player.displayClientMessage(new TranslationTextComponent(fluidStack.getTranslationKey())
                                        .append(new StringTextComponent(": "
                                                + String.format("%,d", fluidStack.getAmount()) + " / "
                                                + String.format("%,d", fluidHandler.getTankCapacity(0)))), true);
                            }
                            return ActionResultType.SUCCESS;
                        }
                    } else {
                        if (!player.isCrouching()
                                && tryEmptyContainer(itemStack, fluidHandler, FluidHelpers.BUCKET_VOLUME, player, false).isSuccess()) {
                            // Move fluid from the item into the tank if not sneaking
                            FluidActionResult result = FluidUtil.tryEmptyContainer(itemStack, fluidHandler, FluidHelpers.BUCKET_VOLUME, player, true);
                            if (result.isSuccess()) {
                                ItemStack drainedItem = result.getResult();
                                if (!player.isCreative()) {
                                    InventoryHelpers.tryReAddToStack(player, itemStack, drainedItem);
                                }
                            }
                            return ActionResultType.SUCCESS;
                        } else if (player.isCrouching()
                                && FluidUtil.tryFillContainer(itemStack, fluidHandler, FluidHelpers.BUCKET_VOLUME, player, false).isSuccess()) {
                            // Move fluid from the tank into the item if sneaking
                            FluidActionResult result = FluidUtil.tryFillContainer(itemStack, fluidHandler, FluidHelpers.BUCKET_VOLUME, player, true);
                            if (result.isSuccess()) {
                                ItemStack filledItem = result.getResult();
                                if (!player.isCreative()) {
                                    InventoryHelpers.tryReAddToStack(player, itemStack, filledItem);
                                }
                            }
                            return ActionResultType.SUCCESS;
                        }
                    }
                    return ActionResultType.PASS;
                })
                .orElse(ActionResultType.PASS);
    }

    // A modified/fixed version of FluidUtil#tryEmptyContainer
    // TODO: Remove this once Forge fixes it.
    @Nonnull
    public static FluidActionResult tryEmptyContainer(@Nonnull ItemStack container, IFluidHandler fluidDestination, int maxAmount, @Nullable PlayerEntity player, boolean doDrain)
    {
        ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1); // do not modify the input
        return FluidUtil.getFluidHandler(containerCopy)
                .map(containerFluidHandler -> {
                    FluidStack transfer = FluidUtil.tryFluidTransfer(fluidDestination, containerFluidHandler, maxAmount, doDrain);
                    if (transfer.isEmpty())
                        return FluidActionResult.FAILURE;

                    if (doDrain && player != null)
                    {
                        SoundEvent soundevent = transfer.getFluid().getAttributes().getEmptySound(transfer);
                        player.level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }

                    ItemStack resultContainer = containerFluidHandler.getContainer();
                    return new FluidActionResult(resultContainer);
                })
                .orElse(FluidActionResult.FAILURE);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
        return TileHelpers.getSafeTile(worldIn, pos, TileFlopper.class)
                .map(tile -> tile.getTank().getFluidAmount() * 8 / tile.getTank().getCapacity())
                .orElse(0);
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        // Force allow shift-right clicking with a fluid container passing through to this block
        if (!event.getItemStack().isEmpty()
                && event.getWorld().getBlockState(event.getPos()).getBlock() == this
                && event.getItemStack().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent()) {
            event.setUseBlock(Event.Result.ALLOW);
        }
    }

}
