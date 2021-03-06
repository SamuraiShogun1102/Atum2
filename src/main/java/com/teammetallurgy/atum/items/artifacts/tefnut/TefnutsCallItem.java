package com.teammetallurgy.atum.items.artifacts.tefnut;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.client.render.ItemStackRenderer;
import com.teammetallurgy.atum.entity.projectile.arrow.TefnutsCallEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TefnutsCallItem extends Item {

    public TefnutsCallItem() {
        super(new Item.Properties().maxDamage(650).rarity(Rarity.RARE).group(Atum.GROUP).setISTER(() -> ItemStackRenderer::new));
        this.addPropertyOverride(new ResourceLocation("throwing"), (stack, world, player) -> player != null && player.isHandActive() && player.getActiveItemStack() == stack ? 1.0F : 0.0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack stack) {
        return 7200;
    }

    @Override
    @Nonnull
    public UseAction getUseAction(@Nonnull ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public boolean canPlayerBreakBlockWhileHolding(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, PlayerEntity player) {
        return !player.isCreative();
    }

    @Override
    public void onPlayerStoppedUsing(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entityLiving;
            int useDuration = this.getUseDuration(stack) - timeLeft;
            if (useDuration > 21) {
                useDuration = 21;
            }

            if (!world.isRemote) {
                stack.damageItem(1, player, (entity) -> {
                    entity.sendBreakAnimation(entityLiving.getActiveHand());
                });

                TefnutsCallEntity spear = new TefnutsCallEntity(world, player, stack);
                spear.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, (float) useDuration / 25.0F + 0.25F, 1.0F);
                spear.setDamage(spear.getDamage() * 2.0D);
                if (player.abilities.isCreativeMode) {
                    spear.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                }

                world.addEntity(spear);
                world.playMovingSound(null, spear, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                if (!player.abilities.isCreativeMode) {
                    player.inventory.deleteStack(stack);
                }
            }
            player.addStat(Stats.ITEM_USED.get(this));
        }
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.getDamage() >= heldStack.getMaxDamage() - 1) {
            return ActionResult.resultFail(heldStack);
        } else {
            player.setActiveHand(hand);
            return ActionResult.resultConsume(heldStack);
        }
    }

    @Override
    public boolean hitEntity(@Nonnull ItemStack stack, @Nonnull LivingEntity target, @Nonnull LivingEntity attacker) {
        stack.damageItem(1, attacker, (entity) -> {
            entity.sendBreakAnimation(EquipmentSlotType.MAINHAND);
        });
        return true;
    }

    @Override
    public boolean onBlockDestroyed(@Nonnull ItemStack stack, @Nonnull World world, BlockState state, @Nonnull BlockPos pos, @Nonnull LivingEntity entityLiving) {
        if ((double) state.getBlockHardness(world, pos) != 0.0D) {
            stack.damageItem(2, entityLiving, (entity) -> {
                entity.sendBreakAnimation(EquipmentSlotType.MAINHAND);
            });
        }
        return true;
    }

    @Override
    public boolean getIsRepairable(@Nonnull ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == Items.DIAMOND;
    }

    @Override
    @Nonnull
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, @Nonnull ItemStack stack) {
        Multimap<String, AttributeModifier> map = HashMultimap.create();
        if (slot == EquipmentSlotType.MAINHAND) {
            map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 3.0D, AttributeModifier.Operation.ADDITION));
            map.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.6D, AttributeModifier.Operation.ADDITION));
        }
        return map;
    }
}