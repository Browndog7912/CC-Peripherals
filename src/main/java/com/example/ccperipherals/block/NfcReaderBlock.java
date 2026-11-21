package com.example.ccperipherals.block;

import com.example.ccperipherals.nfc.NfcCardData;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ItemScatterer;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Right-click with an NFC card to insert it; right-click with an empty hand to take it back out.
 * Computers read and write the inserted card through the {@code nfc_reader} peripheral.
 */
public class NfcReaderBlock extends Block implements BlockEntityProvider {
    public static final BooleanProperty HAS_CARD = BooleanProperty.of("has_card");

    public NfcReaderBlock() {
        super(FabricBlockSettings.create()
            .mapColor(MapColor.STONE_GRAY)
            .strength(2.0f, 6.0f)
            .sounds(BlockSoundGroup.STONE));
        setDefaultState(getStateManager().getDefaultState().with(HAS_CARD, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HAS_CARD);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack held = player.getStackInHand(hand);

        // Nothing to do for an empty hand on an empty reader: let the other hand try.
        if (!state.get(HAS_CARD) && !NfcCardData.isCard(held)) return ActionResult.PASS;
        if (world.isClient) return ActionResult.SUCCESS;

        if (!(world.getBlockEntity(pos) instanceof NfcReaderBlockEntity reader)) return ActionResult.PASS;

        if (reader.hasCard()) {
            if (held.isEmpty()) player.getInventory().offerOrDrop(reader.removeCard());
        } else if (reader.insertCard(held.copyWithCount(1))) {
            if (!player.getAbilities().creativeMode) held.decrement(1);
        }
        return ActionResult.SUCCESS;
    }

    /** Give back the inserted card when the reader is broken. */
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof NfcReaderBlockEntity reader
            && reader.hasCard()) {
            ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, reader.takeCardSilently());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NfcReaderBlockEntity(pos, state);
    }
}
