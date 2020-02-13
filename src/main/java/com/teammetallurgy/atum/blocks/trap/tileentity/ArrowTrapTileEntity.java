package com.teammetallurgy.atum.blocks.trap.tileentity;

import com.teammetallurgy.atum.blocks.trap.TrapBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class ArrowTrapTileEntity extends TrapTileEntity {
    private int timer = 80;

    @Override
    public void tick() {
        boolean isBurningCheck = this.isBurning();
        boolean isBurning = false;
        boolean canDamageEntity = false;

        if (timer > 0) timer--;
        if (!this.isDisabled && this.isBurning()) {
            Direction facing = world.getBlockState(pos).get(TrapBlock.FACING);
            Class<? extends LivingEntity> entity;
            if (this.isInsidePyramid) {
                entity = PlayerEntity.class;
            } else {
                entity = LivingEntity.class;
            }
            AxisAlignedBB box = getFacingBoxWithRange(facing, 13).shrink(1);
            RayTraceResult findBlock = this.rayTraceMinMax(world, box);
            List<LivingEntity> entities = world.getEntitiesWithinAABB(entity, box);
            for (LivingEntity livingBase : entities) {
                boolean cantSeeEntity = findBlock != null && this.getDistance(findBlock.getBlockPos()) < this.getDistance(livingBase.getPosition());
                if (livingBase instanceof PlayerEntity ? !((PlayerEntity) livingBase).capabilities.isCreativeMode && !cantSeeEntity : livingBase != null && !cantSeeEntity) {
                    if (canSee(facing, livingBase)) {
                        canDamageEntity = true;
                        if (timer == 0) {
                            timer = 80;
                            this.triggerTrap(facing, livingBase);
                        }
                    } else {
                        canDamageEntity = false;
                    }
                } else {
                    canDamageEntity = false;
                }
            }
        }

        //Copied from TileEntityTrap
        if (this.isInsidePyramid) {
            this.burnTime = 1;
        }

        if (this.isBurning() && !this.isDisabled && canDamageEntity && !this.isInsidePyramid) {
            --this.burnTime;
        }

        if (!this.world.isRemote && !this.isDisabled) {
            ItemStack fuel = this.inventory.get(0);
            if (this.isBurning() || !fuel.isEmpty()) {
                if (!this.isBurning()) {
                    this.burnTime = TileEntityFurnace.getItemBurnTime(fuel) / 10;
                    this.currentItemBurnTime = this.burnTime;
                    if (this.isBurning()) {
                        isBurning = true;
                        if (!fuel.isEmpty()) {
                            fuel.shrink(1);
                        }
                    }
                }
            }
            if (isBurningCheck != this.isBurning()) {
                isBurning = true;
            }
        }
        if (isBurning) {
            this.markDirty();
        }
    }

    private boolean canSee(Direction facing, World world, LivingEntity living){
        Vec3i dir = facing.getDirectionVec();
        return world.rayTraceBlocks(new Vec3d(pos.getX() + dir.getX(), pos.getY() + dir.getY(), pos.getZ() + dir.getZ()), new Vec3d(living.posX, living.posY + (double)living.getEyeHeight(), living.posZ), true, true, false) == null;
    }

    private RayTraceResult rayTraceMinMax(World world, AxisAlignedBB box) {
        final Vec3d min = new Vec3d(box.minX, box.minY, box.minZ);
        final Vec3d max = new Vec3d(box.maxX, box.maxY + 0.05D, box.maxZ);
        return world.rayTraceBlocks(max, min, true, true, false);
    }

    private double getDistance(BlockPos position) {
        double d0 = position.getX() - this.pos.getX();
        double d1 = position.getY() - this.pos.getY();
        double d2 = position.getZ() - this.pos.getZ();
        return (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }
    
    @Override
    protected void triggerTrap(Direction facing, LivingEntity livingBase) {
        double x = (double) pos.getX() + 0.5D;
        double y = (double) pos.getY() + world.rand.nextDouble() * 12.0D / 16.0D;
        double z = (double) pos.getZ() + 0.5D;
        double randomPos = world.rand.nextDouble() * 0.6D - 0.3D;

        world.playSound(x, (double) pos.getY(), z, SoundEvents.BLOCK_DISPENSER_LAUNCH, SoundCategory.BLOCKS, 1.0F, 1.2F, false);

        switch (facing) {
            case DOWN:
                fireArrow(world, facing, x - randomPos, y - 0.5D, z);
                world.addParticle(ParticleTypes.SMOKE, x - randomPos, y - 0.2D, z, 0.0D, 0.0D, 0.0D);
                break;
            case UP:
                fireArrow(world, facing, x - randomPos, y + 1.0D, z);
                world.addParticle(ParticleTypes.SMOKE, x - randomPos, y + 1.0D, z, 0.0D, 0.0D, 0.0D);
                break;
            case WEST:
                fireArrow(world, facing, x - 0.52D, y, z + randomPos);
                world.addParticle(ParticleTypes.SMOKE, x - 0.52D, y, z + randomPos, 0.0D, 0.0D, 0.0D);
                break;
            case EAST:
                fireArrow(world, facing, x + 0.52D, y, z + randomPos);
                world.addParticle(ParticleTypes.SMOKE, x + 0.52D, y, z + randomPos, 0.0D, 0.0D, 0.0D);
                break;
            case NORTH:
                fireArrow(world, facing, x + randomPos, y, z - 0.52D);
                world.addParticle(ParticleTypes.SMOKE, x + randomPos, y, z - 0.52D, 0.0D, 0.0D, 0.0D);
                break;
            case SOUTH:
                fireArrow(world, facing, x + randomPos, y, z + 0.52D);
                world.addParticle(ParticleTypes.SMOKE, x + randomPos, y, z + 0.52D, 0.0D, 0.0D, 0.0D);
                break;
        }
    }

    private void fireArrow(World world, Direction facing, double x, double y, double z) {
        if (!world.isRemote) {
            ArrowEntity arrow = new ArrowEntity(world, x, y, z);
            arrow.shoot(facing.getXOffset(), (float) facing.getYOffset() + 0.1F, facing.getZOffset(), 1.1F, 6.0F);
            world.addEntity(arrow);
        }
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.timer = compound.getInt("Timer");
    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("Timer", this.timer);
        return compound;
    }
}