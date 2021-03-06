package com.teammetallurgy.atum.blocks.wood;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Random;

public class AtumWallTorchUnlitBlock extends AtumWallTorch {
    protected final Block litWallBlock;
    public static final Map<Block, Block> UNLIT = Maps.newHashMap();

    public AtumWallTorchUnlitBlock(Block litWallBlock, Properties properties) {
        super(properties);
        this.litWallBlock = litWallBlock;
    }

    @Override
    @Nonnull
    public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult rayTraceResult) { //Copied from AtumTorchUnlitBlock
        ItemStack heldStack = player.getHeldItem(hand);
        Block block = Block.getBlockFromItem(heldStack.getItem());
        if ((heldStack.getItem() instanceof FlintAndSteelItem || block.getLightValue(block.getDefaultState(), world, pos) > 0)) {
            if (heldStack.getItem().isDamageable()) {
                heldStack.damageItem(1, player, (p) -> p.sendBreakAnimation(hand));
            }
            world.setBlockState(pos, this.litWallBlock.getDefaultState().with(HORIZONTAL_FACING, state.get(HORIZONTAL_FACING))); //This line is changed, compared to AtumTorchUnlitBlock
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 2.5F, 1.0F);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Random rand) {
    }
}