package com.palm1.analogaudio.block;

import com.palm1.analogaudio.block.entity.RadioBlockEntity;
import com.palm1.analogaudio.client.audio.ClientAudioEngine;
import com.palm1.analogaudio.item.CassetteData;
import com.palm1.analogaudio.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class RadioBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 4.0D, 14.0D, 11.0D, 12.0D);

    public RadioBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(POWERED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadioBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RadioBlockEntity radio) {
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHooks.openScreen(serverPlayer, radio, pos);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RadioBlockEntity radio) {
                radio.drops();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return createTickerHelper(blockEntityType, ModBlockEntities.RADIO.get(), (lvl, pos, st, entity) -> {
                ItemStack cassette = entity.getCassette();
                if (cassette != null && !cassette.isEmpty() && entity.getStartTime() != 0) {
                    CassetteData data = CassetteData.get(cassette);
                    if (data != null) {
                        ClientAudioEngine.tickRadio(pos, Vec3.atCenterOf(pos), data,
                                entity.getStartTime(), entity.getVolume(), entity.isLooping());

                        if (lvl.getGameTime() % 10 == 0 && entity.isPlaying()) {
                            double x = pos.getX() + 0.5 + (lvl.random.nextDouble() - 0.5) * 0.4;
                            double y = pos.getY() + 0.8;
                            double z = pos.getZ() + 0.5 + (lvl.random.nextDouble() - 0.5) * 0.4;

                            int r = (data.color() >> 16) & 0xFF;
                            int g = (data.color() >> 8) & 0xFF;
                            int b = data.color() & 0xFF;
                            float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
                            float colorOffset = (0.33f - hsb[0] + 1.0f) % 1.0f;

                            lvl.addParticle(ParticleTypes.NOTE, x, y, z, colorOffset, 0, 0);
                        }
                        return;
                    }
                }
                ClientAudioEngine.stopRadio(pos);
            });
        }
        return null;
    }
}
