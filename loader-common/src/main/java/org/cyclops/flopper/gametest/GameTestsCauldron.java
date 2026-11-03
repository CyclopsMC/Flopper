package org.cyclops.flopper.gametest;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import org.cyclops.flopper.RegistryEntries;
import org.cyclops.flopper.block.BlockFlopper;

/**
 * Cauldron tests, only for loaders that expose cauldrons as fluid storage.
 * @author rubensworks
 */
public class GameTestsCauldron extends GameTestsCommon {

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFillFromWaterCauldron(GameTestHelper helper) {
        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Block output position of flopper, so it can't push to world
        helper.setBlock(POS.offset(2, 2, 1), Blocks.STONE
                .defaultBlockState());

        // Full water cauldron above flopper
        helper.setBlock(POS.offset(2, 3, 2), Blocks.WATER_CAULDRON
                .defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL));

        helper.succeedWhen(() -> {
            // Flopper must contain fluid
            assertFlopperHasBucket(helper, POS.offset(2, 2, 2));

            // Cauldron above flopper must be empty
            helper.assertBlockPresent(Blocks.CAULDRON, POS.offset(2, 3, 2));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFillFromLavaCauldron(GameTestHelper helper) {
        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Block output position of flopper, so it can't push to world
        helper.setBlock(POS.offset(2, 2, 1), Blocks.STONE
                .defaultBlockState());

        // Lava cauldron above flopper
        helper.setBlock(POS.offset(2, 3, 2), Blocks.LAVA_CAULDRON
                .defaultBlockState());

        helper.succeedWhen(() -> {
            // Flopper must contain fluid
            assertFlopperHasBucket(helper, POS.offset(2, 2, 2));

            // Cauldron above flopper must be empty
            helper.assertBlockPresent(Blocks.CAULDRON, POS.offset(2, 3, 2));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFillFromWaterCauldronIntoLava(GameTestHelper helper) {
        // Water and lava should not mix, and the cauldron must not be drained

        helper.setBlock(POS.offset(2, 2, 2), RegistryEntries.BLOCK_FLOPPER.value()
                .defaultBlockState()
                .setValue(BlockFlopper.FACING, Direction.NORTH));

        // Block output position of flopper, so it can't push to world
        helper.setBlock(POS.offset(2, 2, 1), Blocks.STONE
                .defaultBlockState());

        // Fill with lava
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LAVA_BUCKET));
        helper.useBlock(POS.offset(2, 2, 2), player);

        // Full water cauldron above flopper
        helper.setBlock(POS.offset(2, 3, 2), Blocks.WATER_CAULDRON
                .defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL));

        helper.runAfterDelay(40, () -> {
            // Flopper must still only contain the lava
            assertFlopperHasBucket(helper, POS.offset(2, 2, 2));

            // Cauldron above flopper must still be full
            helper.assertBlockProperty(POS.offset(2, 3, 2), LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL);

            helper.succeed();
        });
    }

}
