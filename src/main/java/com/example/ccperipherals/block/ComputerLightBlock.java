package com.example.ccperipherals.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/** A lamp computers can switch on and off. Its brightness (0 to 15) is part of its block state. */
public class ComputerLightBlock extends Block implements BlockEntityProvider {
    public static final IntProperty LIGHT_LEVEL = IntProperty.of("light_level", 0, 15);

    public ComputerLightBlock() {
        super(FabricBlockSettings.create()
            .mapColor(MapColor.YELLOW)
            .strength(0.5f)
            .sounds(BlockSoundGroup.GLASS)
            .luminance(state -> state.get(LIGHT_LEVEL)));
        setDefaultState(getStateManager().getDefaultState().with(LIGHT_LEVEL, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ComputerLightBlockEntity(pos, state);
    }
}
