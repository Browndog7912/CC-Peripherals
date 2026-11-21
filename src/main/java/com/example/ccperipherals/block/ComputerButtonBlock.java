package com.example.ccperipherals.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
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

/** Right-click it and attached computers get a {@code button_click} event. */
public class ComputerButtonBlock extends Block implements BlockEntityProvider {
    public static final BooleanProperty PRESSED = BooleanProperty.of("pressed");
    private static final int PRESS_TICKS = 10;

    public ComputerButtonBlock() {
        super(FabricBlockSettings.create()
            .mapColor(MapColor.STONE_GRAY)
            .strength(2.0f, 6.0f)
            .sounds(BlockSoundGroup.STONE));
        setDefaultState(getStateManager().getDefaultState().with(PRESSED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PRESSED);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // While the button is still pressed in, extra clicks are ignored, which also stops event spam.
        if (world.isClient || state.get(PRESSED)) return ActionResult.SUCCESS;

        world.setBlockState(pos, state.with(PRESSED, true), Block.NOTIFY_ALL);
        world.scheduleBlockTick(pos, this, PRESS_TICKS);
        world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3f, 0.6f);

        if (player instanceof ServerPlayerEntity serverPlayer
            && world.getBlockEntity(pos) instanceof ComputerButtonBlockEntity button) {
            button.getPeripheral().onClick(serverPlayer);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(PRESSED)) {
            world.setBlockState(pos, state.with(PRESSED, false), Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3f, 0.5f);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ComputerButtonBlockEntity(pos, state);
    }
}
