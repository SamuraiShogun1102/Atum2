package com.teammetallurgy.atum.entity.projectile;

import com.teammetallurgy.atum.entity.animal.CamelEntity;
import com.teammetallurgy.atum.init.AtumEntities;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.UUID;

public class CamelSpitEntity extends LlamaSpitEntity {
    private CamelEntity owner;
    private CompoundNBT ownerNbt;

    public CamelSpitEntity(FMLPlayMessages.SpawnEntity spawnPacket, World world) {
        this(AtumEntities.CAMEL_SPIT, world);
    }

    public CamelSpitEntity(EntityType<? extends CamelSpitEntity> entityType, World world) {
        super(entityType, world);
    }

    public CamelSpitEntity(World world, CamelEntity camel) {
        this(AtumEntities.CAMEL_SPIT, world);
        this.owner = camel;
        this.setPosition(camel.getPosX() - (double) (camel.getWidth() + 1.0F) * 0.5D * (double) MathHelper.sin(camel.renderYawOffset * ((float) Math.PI / 180F)), camel.getPosY() + (double) camel.getEyeHeight() - (double) 0.1F, camel.getPosZ() + (double) (camel.getWidth() + 1.0F) * 0.5D * (double) MathHelper.cos(camel.renderYawOffset * ((float) Math.PI / 180F)));
    }

    @OnlyIn(Dist.CLIENT)
    public CamelSpitEntity(World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        this(AtumEntities.CAMEL_SPIT, world);

        for (int i = 0; i < 7; ++i) {
            double d0 = 0.4D + 0.1D * (double) i;
            world.addParticle(ParticleTypes.SPIT, x, y, z, xSpeed * d0, ySpeed, zSpeed * d0);
        }
        this.setMotion(xSpeed, ySpeed, zSpeed);
    }

    @Override
    @Nonnull
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ownerNbt != null) {
            this.restoreOwnerFromSave();
        }

        Vec3d motion = this.getMotion();
        RayTraceResult raytrace = ProjectileHelper.rayTrace(this, this.getBoundingBox().expand(motion).grow(1.0D), (e) -> !e.isSpectator() && e != this.owner, RayTraceContext.BlockMode.OUTLINE, true);
        if (raytrace != null && !ForgeEventFactory.onProjectileImpact(this, raytrace)) {
            this.onHit(raytrace);
        }

        double x = this.getPosX() + motion.x;
        double y = this.getPosY() + motion.y;
        double z = this.getPosZ() + motion.z;
        float f = MathHelper.sqrt(horizontalMag(motion));
        this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI));

        for (this.rotationPitch = (float) (MathHelper.atan2(motion.y, f) * (double) (180F / (float) Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
        this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);
        if (!this.world.isMaterialInBB(this.getBoundingBox(), Material.AIR)) {
            this.remove();
        } else if (this.isInWaterOrBubbleColumn()) {
            this.remove();
        } else {
            this.setMotion(motion.scale(0.99F));
            if (!this.hasNoGravity()) {
                this.setMotion(this.getMotion().add(0.0D, -0.06F, 0.0D));
            }
            this.setPosition(x, y, z);
        }
    }

    @Override
    public void onHit(RayTraceResult rayTrace) {
        RayTraceResult.Type type = rayTrace.getType();
        if (type == RayTraceResult.Type.ENTITY && this.owner != null) {
            ((EntityRayTraceResult) rayTrace).getEntity().attackEntityFrom(DamageSource.causeIndirectDamage(this, this.owner).setProjectile(), 1.0F);
        } else if (type == RayTraceResult.Type.BLOCK && !this.world.isRemote) {
            this.remove();
        }
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        if (compound.contains("Owner", 10)) {
            this.ownerNbt = compound.getCompound("Owner");
        }
    }

    @Override
    protected void writeAdditional(@Nonnull CompoundNBT compound) {
        if (this.owner != null) {
            CompoundNBT nbttagcompound = new CompoundNBT();
            UUID uuid = this.owner.getUniqueID();
            nbttagcompound.putUniqueId("OwnerUUID", uuid);
            compound.put("Owner", nbttagcompound);
        }
    }

    private void restoreOwnerFromSave() {
        if (this.ownerNbt != null && this.ownerNbt.hasUniqueId("OwnerUUID")) {
            UUID uuid = this.ownerNbt.getUniqueId("OwnerUUID");

            for (CamelEntity entitycamel : this.world.getEntitiesWithinAABB(CamelEntity.class, this.getBoundingBox().grow(15.0D))) {
                if (entitycamel.getUniqueID().equals(uuid)) {
                    this.owner = entitycamel;
                    break;
                }
            }
        }
        this.ownerNbt = null;
    }
}