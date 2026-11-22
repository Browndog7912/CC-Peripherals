package com.example.ccperipherals.block;

import com.example.ccperipherals.CCPeripherals;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * An air conditioner with a temperature screen on its front. It is controlled by computers through the
 * {@code air_conditioner} peripheral. All of its settings are block state, so they are saved with the block
 * and the client can see them without any extra networking.
 */
public class AirConditionerBlock extends Block implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    /** Whether the air conditioner is running (screen lit, particles and fan sound possible). */
    public static final BooleanProperty ON = BooleanProperty.of("on");
    /** Whether the air particles are shown while running. */
    public static final BooleanProperty PARTICLES = BooleanProperty.of("particles");
    /** Whether the fan noise plays while running. */
    public static final BooleanProperty SOUND = BooleanProperty.of("sound");

    public AirConditionerBlock() {
        super(FabricBlockSettings.create()
            .mapColor(MapColor.IRON_GRAY)
            .strength(2.0f, 6.0f)
            .sounds(BlockSoundGroup.METAL));
        setDefaultState(getStateManager().getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(ON, true)
            .with(PARTICLES, true)
            .with(SOUND, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ON, PARTICLES, SOUND);
    }

    /** The front (screen and vent) points back at the player who placed the block. */
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AirConditionerBlockEntity(pos, state);
    }

    /** Particles and sound are driven from the server, so only the server needs a ticker. */
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient || type != CCPeripherals.AIR_CONDITIONER_ENTITY) return null;
        BlockEntityTicker<AirConditionerBlockEntity> ticker = AirConditionerBlockEntity::serverTick;
        return (BlockEntityTicker<T>) (Object) ticker;
    }
}
