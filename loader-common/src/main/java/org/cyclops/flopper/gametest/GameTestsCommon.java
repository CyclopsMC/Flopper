package org.cyclops.flopper.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.cyclops.flopper.Reference;
import org.cyclops.flopper.RegistryEntries;
import org.cyclops.flopper.block.BlockFlopper;
import org.cyclops.flopper.blockentity.BlockEntityFlopper;

/**
 * @author rubensworks
 */
public class GameTestsCommon {

    public static final String TEMPLATE_EMPTY = Reference.MOD_ID + ":empty10";
    public static final BlockPos POS = BlockPos.ZERO;

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPlacementDirection(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_FLOPPER.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS, Direction.SOUTH);

        helper.succeedIf(() -> {
            helper.assertBlockPresent(RegistryEntries.BLOCK_FLOPPER.value(), POS.south());
            helper.assertBlockProperty(POS.south(), BlockFlopper.FACING, Direction.NORTH);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPlacementDirectionDown(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_FLOPPER.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS, Direction.UP);

        helper.succeedIf(() -> {
            helper.assertBlockPresent(RegistryEntries.BLOCK_FLOPPER.value(), POS.above());
            helper.assertBlockProperty(POS.above(), BlockFlopper.FACING, Direction.DOWN);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFillFromBucket(GameTestHelper helper) {
        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Block output position of flopper, so it can't push to world
        helper.setBlock(POS.offset(2, 2, 1), Blocks.STONE
                .defaultBlockState());

        // Right click with water bucket
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(Items.WATER_BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.useBlock(POS.offset(2, 2, 2), player);

        helper.succeedWhen(() -> {
            // Flopper must contain fluid
            assertFlopperHasBucket(helper, POS.offset(2, 2, 2));

            // Bucket item must be empty
            assertItemIs(player.getItemInHand(InteractionHand.MAIN_HAND), new ItemStack(Items.BUCKET));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testEmptyToBucket(GameTestHelper helper) {
        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Block output position of flopper, so it can't push to world
        helper.setBlock(POS.offset(2, 2, 1), Blocks.STONE
                .defaultBlockState());

        // Fill with fluid
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStackFill = new ItemStack(Items.WATER_BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStackFill);
        helper.useBlock(POS.offset(2, 2, 2), player);

        // Right click with empty bucket
        ItemStack itemStackDrain = new ItemStack(Items.BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStackDrain);
        player.setPose(Pose.CROUCHING);
        helper.useBlock(POS.offset(2, 2, 2), player);

        helper.succeedWhen(() -> {
            // Flopper must contain fluid
            assertFlopperIsEmpty(helper, POS.offset(2, 2, 2));

            // Bucket item must be filled
            assertItemIs(player.getItemInHand(InteractionHand.MAIN_HAND), new ItemStack(Items.WATER_BUCKET));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFillFromWorld(GameTestHelper helper) {
        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Block output position of flopper, so it can't push to world
        helper.setBlock(POS.offset(2, 2, 1), Blocks.STONE
                .defaultBlockState());

        // Water block above flopper
        helper.setBlock(POS.offset(2, 3, 2), Blocks.WATER
                .defaultBlockState());

        helper.succeedWhen(() -> {
            // Flopper must contain fluid
            assertFlopperHasBucket(helper, POS.offset(2, 2, 2));

            // Water block above flopper must be gone
            helper.assertBlockNotPresent(Blocks.WATER, POS.offset(2, 3, 2));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFillFromWorldLava(GameTestHelper helper) {
        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Block output position of flopper, so it can't push to world
        helper.setBlock(POS.offset(2, 2, 1), Blocks.STONE
                .defaultBlockState());

        // Lava block above flopper
        helper.setBlock(POS.offset(2, 3, 2), Blocks.LAVA
                .defaultBlockState());

        helper.succeedWhen(() -> {
            // Flopper must contain fluid
            assertFlopperHasBucket(helper, POS.offset(2, 2, 2));

            // Lava block above flopper must be gone
            helper.assertBlockNotPresent(Blocks.LAVA, POS.offset(2, 3, 2));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testEmptyToWorld(GameTestHelper helper) {
        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Water block above flopper
        helper.setBlock(POS.offset(2, 3, 2), Blocks.WATER
                .defaultBlockState());

        helper.succeedWhen(() -> {
            // Water block must be at target position
            helper.assertBlockPresent(Blocks.WATER, POS.offset(2, 2, 1));

            // Flopper must not contain fluid
            assertFlopperIsEmpty(helper, POS.offset(2, 2, 2));

            // Water block above flopper must be gone
            helper.assertBlockNotPresent(Blocks.WATER, POS.offset(2, 3, 2));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 1000)
    public void testFillFromFlopper(GameTestHelper helper) {
        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Place another flopper on top of our flopper
        helper.setBlock(POS.offset(2, 3, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Block output position of flopper, so it can't push to world
        helper.setBlock(POS.offset(2, 2, 1), Blocks.STONE
                .defaultBlockState());

        // Water block above top flopper
        helper.setBlock(POS.offset(2, 4, 2), Blocks.WATER
                .defaultBlockState());

        helper.succeedWhen(() -> {
            // Flopper must contain fluid
            assertFlopperHasBucket(helper, POS.offset(2, 2, 2));

            // Water block above flopper must be gone
            helper.assertBlockNotPresent(Blocks.WATER, POS.offset(2, 4, 2));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 1000)
    public void testEmptyToFlopper(GameTestHelper helper) {
        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Place another flopper at the output location of our flopper
        helper.setBlock(POS.offset(2, 2, 1), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.DOWN));

        // Block output position of the other flopper, so it can't push to world
        helper.setBlock(POS.offset(2, 1, 1), Blocks.STONE
                .defaultBlockState());

        // Water block above main flopper
        helper.setBlock(POS.offset(2, 3, 2), Blocks.WATER
                .defaultBlockState());

        helper.succeedWhen(() -> {
            // Water block above flopper must be gone
            helper.assertBlockNotPresent(Blocks.WATER, POS.offset(2, 3, 2));

            // Flopper must not contain fluid
            assertFlopperIsEmpty(helper, POS.offset(2, 2, 2));

            // Target flopper must contain fluid
            assertFlopperHasBucket(helper, POS.offset(2, 2, 1));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFillFromInfiniteWaterPool(GameTestHelper helper) {
        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Block output position of flopper, so it can't push to world
        helper.setBlock(POS.offset(2, 2, 1), Blocks.STONE
                .defaultBlockState());

        // Infinite water pool above flopper
        helper.setBlock(POS.offset(2, 2, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(POS.offset(2, 2, 3), Blocks.STONE.defaultBlockState());
        helper.setBlock(POS.offset(2, 3, 0), Blocks.STONE.defaultBlockState());
        helper.setBlock(POS.offset(2, 3, 1), Blocks.WATER.defaultBlockState());
        helper.setBlock(POS.offset(2, 3, 2), Blocks.WATER.defaultBlockState());
        helper.setBlock(POS.offset(2, 3, 3), Blocks.WATER.defaultBlockState());
        helper.setBlock(POS.offset(2, 3, 4), Blocks.STONE.defaultBlockState());
        helper.setBlock(POS.offset(1, 3, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(POS.offset(1, 3, 2), Blocks.STONE.defaultBlockState());
        helper.setBlock(POS.offset(1, 3, 3), Blocks.STONE.defaultBlockState());
        helper.setBlock(POS.offset(3, 3, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(POS.offset(3, 3, 2), Blocks.STONE.defaultBlockState());
        helper.setBlock(POS.offset(3, 3, 3), Blocks.STONE.defaultBlockState());

        helper.succeedWhen(() -> {
            // Flopper must contain fluid
            assertFlopperHasBucket(helper, POS.offset(2, 2, 2));

            // Water block above flopper must be gone
            helper.assertBlockNotPresent(Blocks.WATER, POS.offset(2, 3, 2));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFillFromWorldWaterLava(GameTestHelper helper) {
        // Water and lava should not mix

        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Block output position of flopper, so it can't push to world
        helper.setBlock(POS.offset(2, 2, 1), Blocks.STONE
                .defaultBlockState());

        // Fill with lava
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStackFill = new ItemStack(Items.LAVA_BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStackFill);
        helper.useBlock(POS.offset(2, 2, 2), player);

        // Water block above flopper
        helper.setBlock(POS.offset(2, 3, 2), Blocks.WATER
                .defaultBlockState());

        helper.succeedWhen(() -> {
            // Flopper must contain fluid
            assertFlopperHasBucket(helper, POS.offset(2, 2, 2));

            // Bucket item must be empty
            assertItemIs(player.getItemInHand(InteractionHand.MAIN_HAND), new ItemStack(Items.BUCKET));

            // Water block above flopper must still be there
            helper.assertBlockPresent(Blocks.WATER, POS.offset(2, 3, 2));
        });
    }

    protected void assertFlopperHasBucket(GameTestHelper helper, BlockPos pos) {
        helper.assertBlockEntityData(pos, BlockEntityFlopper::hasBucket, () -> "Flopper does not contain a bucket");
    }

    protected void assertFlopperIsEmpty(GameTestHelper helper, BlockPos pos) {
        helper.assertBlockEntityData(pos, (BlockEntityFlopper blockEntity) -> blockEntity.getFluidAmount() == 0, () -> "Flopper is not empty");
    }

    protected void assertItemIs(ItemStack stackExpected, ItemStack stackActual) {
        if (!ItemStack.isSameItemSameComponents(stackExpected, stackActual)) {
            throw new GameTestAssertException("Expected item is not equal to actual. Expected: " + stackExpected + "; Actual: " + stackActual);
        }
    }

}
