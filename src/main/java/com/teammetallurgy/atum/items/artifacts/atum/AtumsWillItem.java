package com.teammetallurgy.atum.items.artifacts.atum;

import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.init.AtumItems;
import com.teammetallurgy.atum.init.AtumParticles;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = Atum.MOD_ID)
public class AtumsWillItem extends SwordItem {
    private static final Object2FloatMap<PlayerEntity> cooldown = new Object2FloatOpenHashMap<>();

    public AtumsWillItem() {
        super(ItemTier.DIAMOND, 3, -2.4F, new Item.Properties().group(Atum.GROUP));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    @Nonnull
    public Rarity getRarity(@Nonnull ItemStack stack) {
        return Rarity.RARE;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttack(AttackEntityEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.world.isRemote) return;
        if (event.getTarget() instanceof LivingEntity && ((LivingEntity) event.getTarget()).getCreatureAttribute() == CreatureAttribute.UNDEAD) {
            if (player.getHeldItemMainhand().getItem() == AtumItems.ATUMS_WILL) {
                cooldown.put(player, player.getCooledAttackStrength(0.5F));
            }
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        Entity trueSource = event.getSource().getTrueSource();
        if (trueSource instanceof PlayerEntity && cooldown.containsKey(trueSource)) {
            if (cooldown.getFloat(trueSource) == 1.0F) {
                LivingEntity target = event.getEntityLiving();
                event.setAmount(event.getAmount() * 2);
                if (target.world instanceof ServerWorld) {
                    ServerWorld serverWorld = (ServerWorld) target.world;
                    serverWorld.spawnParticle(AtumParticles.LIGHT_SPARKLE, target.getPosX() + (random.nextDouble() - 0.5D) * (double) target.getWidth(), target.getPosY() + (target.getHeight() / 2), target.getPosZ() + (random.nextDouble() - 0.5D) * (double) target.getWidth(), 16, 0.25D, 0.05D, 0.25D, 0.01D);
                }
            }
            cooldown.removeFloat(trueSource);
        }
    }
}