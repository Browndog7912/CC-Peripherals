package com.example.ccperipherals.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A block computers can send redstone signals out of, and read signals into, on all six sides
 * independently. The {@code powered} state is only for looks: it is true while any side is outputting.
 */
public class RedstonePortBlock extends Block implements BlockEntityProvider {
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");

    public RedstonePortBlock() {
        super(FabricBlockSettings.create()
            .mapColor(MapColor.STONE_GRAY)
            .strength(2.0f, 6.0f)
            .sounds(BlockSoundGroup.STONE));
        setDefaultState(getStateManager().getDefaultState().with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    /**
     * Vanilla passes the direction from the block asking for power towards this block, so the side of
     * this block that faces the asker is the opposite direction.
     */
    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return world.getBlockEntity(pos) instanceof RedstonePortBlockEntity port ? port.getOutput(direction.getOpposite()) : 0;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof RedstonePortBlockEntity port) {
            port.onNeighborChanged();
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RedstonePortBlockEntity(pos, state);
    }
}
