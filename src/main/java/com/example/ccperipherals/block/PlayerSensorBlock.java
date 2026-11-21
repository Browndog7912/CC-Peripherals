package com.example.ccperipherals.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PlayerSensorBlock extends Block implements BlockEntityProvider {
    /**
     * Collision box inset by half a pixel on the sides, like a cactus. Entities are only reported as
     * "colliding" with a block when their hitbox overlaps the block's cell, and a full-size solid
     * collision box would stop players just outside it, so a touch would never register.
     */
    private static final VoxelShape COLLISION = Block.createCuboidShape(0.5, 0.0, 0.5, 15.5, 16.0, 15.5);

    public PlayerSensorBlock() {
        super(FabricBlockSettings.create()
            .mapColor(MapColor.STONE_GRAY)
            .strength(2.0f, 6.0f)
            .sounds(BlockSoundGroup.STONE));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer
            && world.getBlockEntity(pos) instanceof PlayerSensorBlockEntity sensor) {
            sensor.getPeripheral().onClick(serverPlayer, hit.getSide().asString());
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity instanceof ServerPlayerEntity player
            && world.getBlockEntity(pos) instanceof PlayerSensorBlockEntity sensor) {
            sensor.getPeripheral().onTouch(player, world.getTime());
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PlayerSensorBlockEntity(pos, state);
    }
}
