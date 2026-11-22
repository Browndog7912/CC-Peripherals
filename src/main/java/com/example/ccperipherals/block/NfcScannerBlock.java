package com.example.ccperipherals.block;

import com.example.ccperipherals.nfc.NfcCardData;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A keycard scanner. Right-click it holding an NFC card: the card is read and an {@code nfc_scan} event
 * is sent to attached computers, but the card stays in the player's inventory and cannot be written to.
 * Anything that is not an NFC card is ignored.
 */
public class NfcScannerBlock extends Block implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty SCANNED = BooleanProperty.of("scanned");
    private static final int FLASH_TICKS = 10;

    public NfcScannerBlock() {
        super(FabricBlockSettings.create()
            .mapColor(MapColor.STONE_GRAY)
            .strength(2.0f, 6.0f)
            .sounds(BlockSoundGroup.STONE));
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(SCANNED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, SCANNED);
    }

    /** The front (the textured face) points back at the player who placed the block. */
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack held = player.getStackInHand(hand);

        // Only NFC cards do anything here; with anything else, fall through so the other hand can try.
        if (!NfcCardData.isCard(held)) return ActionResult.PASS;
        if (world.isClient || state.get(SCANNED)) return ActionResult.SUCCESS;

        world.setBlockState(pos, state.with(SCANNED, true), Block.NOTIFY_ALL);
        world.scheduleBlockTick(pos, this, FLASH_TICKS);
        world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.4f, 1.6f);

        if (player instanceof ServerPlayerEntity serverPlayer
            && world.getBlockEntity(pos) instanceof NfcScannerBlockEntity scanner) {
            scanner.getPeripheral().onScan(serverPlayer, held);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(SCANNED)) world.setBlockState(pos, state.with(SCANNED, false), Block.NOTIFY_ALL);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NfcScannerBlockEntity(pos, state);
    }
}
