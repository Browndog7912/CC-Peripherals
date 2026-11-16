package com.example.ccperipherals.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class ChatBoxBlock extends Block implements BlockEntityProvider {
    public ChatBoxBlock() {
        super(FabricBlockSettings.create()
            .mapColor(MapColor.STONE_GRAY)
            .strength(2.0f, 6.0f)
            .sounds(BlockSoundGroup.STONE));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChatBoxBlockEntity(pos, state);
    }
}
