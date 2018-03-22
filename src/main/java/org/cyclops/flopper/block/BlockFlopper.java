package org.cyclops.flopper.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.flopper.tileentity.TileFlopper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Fluid hopper block.
 * @author rubensworks
 */
public class BlockFlopper extends ConfigurableBlockContainer {

    @BlockProperty
    public static final PropertyDirection FACING = PropertyDirection.create("facing", side -> side != EnumFacing.UP);
    @BlockProperty(ignore = true)
    public static final PropertyBool ENABLED = PropertyBool.create("enabled");

    // Collision boxes
    protected static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D);
    protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D);
    protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D);

    // Ray trace boxes
    protected static final AxisAlignedBB COL_TOP_AABB = new AxisAlignedBB(0.0D, 0.625D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB COL_MIDDLE_AABB = new AxisAlignedBB(0.25D, 0.25D, 0.25D, 0.75D, 0.625D, 0.75D);
    protected static final AxisAlignedBB COL_SOUTH_AABB = new AxisAlignedBB(0.375D, 0.25D, 0.75D, 0.625D, 0.5D, 1D);
    protected static final AxisAlignedBB COL_NORTH_AABB = new AxisAlignedBB(0.375D, 0.25D, 0.0D, 0.625D, 0.5D, 0.25D);
    protected static final AxisAlignedBB COL_WEST_AABB = new AxisAlignedBB(0.0D, 0.25D, 0.375D, 0.25D, 0.5D, 0.625D);
    protected static final AxisAlignedBB COL_EAST_AABB = new AxisAlignedBB(0.75D, 0.25D, 0.375D, 1.0D, 0.5D, 0.625D);
    protected static final AxisAlignedBB COL_DOWN_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.25D, 0.625D);

    private static BlockFlopper _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static BlockFlopper getInstance() {
        return _instance;
    }

    public BlockFlopper(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, Material.IRON, TileFlopper.class);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean isKeepNBTOnDrop() {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_AABB);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_AABB);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_AABB);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_AABB);
    }

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        if (BlockFlopperConfig.narrowCollision) {
            RayTraceResult result;

            if ((result = rayTrace(pos, start, end, COL_TOP_AABB)) != null) {
                return result;
            }
            if ((result = rayTrace(pos, start, end, COL_MIDDLE_AABB)) != null) {
                return result;
            }

            EnumFacing facing = blockState.getValue(FACING);
            AxisAlignedBB aabb = null;
            switch (facing) {
                case EAST:
                    aabb = COL_EAST_AABB;
                    break;
                case WEST:
                    aabb = COL_WEST_AABB;
                    break;
                case SOUTH:
                    aabb = COL_SOUTH_AABB;
                    break;
                case NORTH:
                    aabb = COL_NORTH_AABB;
                    break;
                case DOWN:
                    aabb = COL_DOWN_AABB;
                    break;
            }

            if (aabb != null && (result = rayTrace(pos, start, end, aabb)) != null) {
                return result;
            }

            return null;
        }
        return super.collisionRayTrace(blockState, worldIn, pos, start, end);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer, EnumHand hand) {
        EnumFacing enumfacing = facing.getOpposite();
        if (enumfacing == EnumFacing.UP) {
            enumfacing = EnumFacing.DOWN;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(ENABLED, true);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        this.updateState(worldIn, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        this.updateState(worldIn, pos, state);
    }

    private void updateState(World worldIn, BlockPos pos, IBlockState state) {
        boolean notPowered = !worldIn.isBlockPowered(pos);
        if (notPowered != state.getValue(ENABLED)) {
            worldIn.setBlockState(pos, state.withProperty(ENABLED, notPowered), 4);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isTopSolid(IBlockState state) {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    @SuppressWarnings("deprecation")
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.UP ? BlockFaceShape.BOWL : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ)) {
            return true;
        }
        IFluidHandler fluidHandler = TileHelpers.getCapability(world, pos, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
        if (fluidHandler != null) {
            ItemStack itemStack = player.getHeldItem(hand);
            if (itemStack.isEmpty()) {
                if (BlockFlopperConfig.showContentsStatusMessageOnClick) {
                    // If the hand is empty, show the tank contents
                    FluidStack fluidStack = fluidHandler.drain(Integer.MAX_VALUE, false);
                    if (fluidStack == null) {
                        player.sendStatusMessage(new TextComponentString("0 / "
                                + String.format("%,d", fluidHandler.getTankProperties()[0].getCapacity())), true);
                    } else {
                        player.sendStatusMessage(new TextComponentString(fluidStack.getLocalizedName() + ": "
                                + String.format("%,d", fluidStack.amount) + " / "
                                + String.format("%,d", fluidHandler.getTankProperties()[0].getCapacity())), true);
                    }
                    return true;
                }
            } else {
                if (!player.isSneaking()
                        && FluidUtil.tryEmptyContainer(itemStack, fluidHandler, Fluid.BUCKET_VOLUME, player, false).isSuccess()) {
                    // Move fluid from the item into the tank if not sneaking
                    ItemStack drainedItem = FluidUtil.tryEmptyContainer(itemStack, fluidHandler, Fluid.BUCKET_VOLUME, player, true).getResult();
                    if (!player.capabilities.isCreativeMode) {
                        InventoryHelpers.tryReAddToStack(player, itemStack, drainedItem);
                    }
                    return true;
                } else if (player.isSneaking()
                        && FluidUtil.tryFillContainer(itemStack, fluidHandler, Fluid.BUCKET_VOLUME, player, false).isSuccess()) {
                    // Move fluid from the tank into the item if sneaking
                    FluidActionResult result = FluidUtil.tryFillContainer(itemStack, fluidHandler, Fluid.BUCKET_VOLUME, player, true);
                    if (result.isSuccess()) {
                        ItemStack filledItem = result.getResult();
                        if (!player.capabilities.isCreativeMode) {
                            InventoryHelpers.tryReAddToStack(player, itemStack, filledItem);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        // Force allow shift-right clicking with a fluid container passing through to this block
        if (!event.getItemStack().isEmpty()
                && event.getWorld().getBlockState(event.getPos()).getBlock() == this
                && event.getItemStack().hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            event.setUseBlock(Event.Result.ALLOW);
        }
    }

}
