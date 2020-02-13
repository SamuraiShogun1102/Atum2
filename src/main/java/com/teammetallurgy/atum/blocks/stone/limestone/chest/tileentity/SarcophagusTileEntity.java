package com.teammetallurgy.atum.blocks.stone.limestone.chest.tileentity;

import com.teammetallurgy.atum.blocks.base.tileentity.ChestBaseTileEntity;
import com.teammetallurgy.atum.blocks.stone.limestone.chest.SarcophagusBlock;
import com.teammetallurgy.atum.entity.undead.PharaohEntity;
import com.teammetallurgy.atum.init.AtumBlocks;
import com.teammetallurgy.atum.init.AtumEntities;
import com.teammetallurgy.atum.init.AtumSounds;
import com.teammetallurgy.atum.utils.AtumUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

public class SarcophagusTileEntity extends ChestBaseTileEntity {
    public static final String SARCOPHAGUS_CONTAINER = "atum.container.sarcophagus";
    public boolean hasSpawned = false;
    public boolean isOpenable = false;

    public SarcophagusTileEntity() {
        super(false, true, AtumBlocks.SARCOPHAGUS);
    }

    @Override
    @Nonnull
    public String getName() {
        return this.hasCustomName() ? this.customName : SARCOPHAGUS_CONTAINER;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager manager, SUpdateTileEntityPacket packet) {
        super.onDataPacket(manager, packet);
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    @Nonnull
    public CompoundNBT getUpdateTag() {
        return this.writeToNBT(new CompoundNBT());
    }

    @Override
    public void readFromNBT(CompoundNBT compound) {
        super.readFromNBT(compound);
        this.hasSpawned = compound.getBoolean("spawned");
        this.isOpenable = compound.getBoolean("openable");
    }

    @Override
    @Nonnull
    public CompoundNBT writeToNBT(@Nonnull CompoundNBT compound) {
        super.writeToNBT(compound);
        compound.putBoolean("spawned", this.hasSpawned);
        compound.putBoolean("openable", this.isOpenable);
        return compound;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull PlayerEntity player) {
        return this.isOpenable && super.isUsableByPlayer(player);
    }

    public void setOpenable() {
        this.isOpenable = true;
        this.markDirty();
        BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    public void spawn(PlayerEntity player, DifficultyInstance difficulty) {
        if (!world.isRemote) {
            PharaohEntity pharaoh = AtumEntities.PHARAOH.create(this.world);
            pharaoh.onInitialSpawn(this.world, difficulty, SpawnReason.TRIGGERED, null, null);
            Direction blockFacing = world.getBlockState(pos).get(SarcophagusBlock.FACING);
            pharaoh.setLocationAndAngles(pos.getX(), pos.getY() + 1, pos.getZ(), blockFacing.getHorizontalAngle() + 90, 0.0F);
            pharaoh.rotationYawHead = blockFacing.getHorizontalAngle() + 90;
            pharaoh.setSarcophagusPos(pos);
            world.addEntity(pharaoh);
            pharaoh.spawnGuards(pharaoh.getPosition().offset(blockFacing, 1).down());
            pharaoh.spawnExplosionParticle();
            this.hasSpawned = true;

            if (this.world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld) this.world;
                for (ServerPlayerEntity playerMP : serverWorld.getServer().getPlayerList().getPlayers()) {
                    playerMP.sendMessage(new StringTextComponent(PharaohEntity.God.getGod(pharaoh.getVariant()).getColor() + pharaoh.getName().getFormattedText() + " " + AtumUtils.format("chat.atum.summonPharaoh") + " " + player.getGameProfile().getName()));
                }
            }
        }
        this.world.playSound(pos.getX(), pos.getY(), pos.getZ(), AtumSounds.PHARAOH_SPAWN, SoundCategory.HOSTILE, 0.8F, 1.0F, true);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return this.isOpenable && super.isItemValidForSlot(index, stack);
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int index, int count) {
        return this.isOpenable ? super.decrStackSize(index, count) : ItemStack.EMPTY;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nonnull Direction direction) {
        return this.isOpenable ? super.getCapability(capability, direction) : LazyOptional.empty();
    }
}