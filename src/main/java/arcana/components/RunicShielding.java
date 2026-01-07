package arcana.components;

import arcana.ArcanaRegistry;
import arcana.ArcanaSounds;
import arcana.aspects.Aspects;
import arcana.items.WandItem;
import arcana.mixin.LivingEntityAccessor;
import arcana.util.InventoryUtil;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

import static arcana.Arcana.arcId;

public class RunicShielding implements Component, AutoSyncedComponent, ServerTickingComponent{
	
	public static final ComponentKey<RunicShielding> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("runic_shielding_user"), RunicShielding.class);
	
	public static final EntityAttribute MAX_SHIELDING = new ClampedEntityAttribute("attribute.name.generic.arcana.max_shielding", 0, 0, 100).setTracked(true);
	
	private static final int MAX_RECHARGE_TIMER = 7 * 20;
	
	public static RunicShielding from(PlayerEntity entity){
		return entity.getComponent(KEY);
	}
	
	public static int getMaxShielding(LivingEntity entity){
		return (int)entity.getAttributeValue(MAX_SHIELDING);
	}
	
	private final PlayerEntity player;
	
	private int halfPoints = 0;
	private int rechargeTimer = 0;
	private long lastRechargeTime = 0;
	private long lastActivateTime = 0;
	
	public RunicShielding(PlayerEntity player){
		this.player = player;
	}
	
	public int getHalfPoints(){
		return halfPoints;
	}
	
	public int getRechargeTimer(){
		return rechargeTimer;
	}
	
	public long getLastRechargeTime(){
		return lastRechargeTime;
	}
	
	public long getLastActivateTime(){
		return lastActivateTime;
	}
	
	public boolean handleDamage(DamageSource source, float amount){
		World world = player.getWorld();
		// TODO: rearrange mixin to avoid needing explicit checks for invulnerability sources (inc. modded ones)?
		if(player.getAbilities().invulnerable
				|| world.isClient
				|| player.isInvulnerableTo(source)
				|| player.isDead()
				|| (source.isFire() && player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))
				|| (player.timeUntilRegen > 10 && amount <= ((LivingEntityAccessor)player).arcana$getLastDamageTaken()))
			return true;
		if(halfPoints <= 1 || source.isOutOfWorld())
			return false;
		float frac = MathHelper.clamp(amount / player.getHealth(), 0, 1);
		float chance = MathHelper.sqrt(frac);
		if(player.getRandom().nextFloat() <= chance){
			boolean hasHeartTrinket = InventoryUtil.hasTrinket(player, ArcanaRegistry.RING_OF_TWIN_HEARTBEATS);
			halfPoints -= 2;
			rechargeTimer = (hasHeartTrinket ? -17 : -10) * 20;
			lastActivateTime = player.world.getTime();
			player.timeUntilRegen = 20;
			((LivingEntityAccessor)player).arcana$setLastDamageTaken(amount);
			world.playSound(
					null,
					player.getX(), player.getY(), player.getZ(),
					ArcanaSounds.RUNIC_SHIELDING_HIT, player.getSoundCategory(),
					0.9f + 0.2f * player.getRandom().nextFloat(),
					0.8f + 0.4f * player.getRandom().nextFloat()
			);
			sync();
			return true;
		}
		return false;
	}
	
	public void serverTick(){
		int maxHalfPoints = getMaxShielding(player) * 2;
		if(halfPoints < 0 || halfPoints > maxHalfPoints){
			halfPoints = MathHelper.clamp(halfPoints, 0, maxHalfPoints);
			sync();
		}
		if(maxHalfPoints == 0)
			rechargeTimer = 0;
		if(halfPoints < maxHalfPoints){
			rechargeTimer++;
			if(player.world.getTime() % 4 == 0 && InventoryUtil.hasTrinket(player, ArcanaRegistry.RING_OF_THE_SURGING_BARRIER)){
				ItemStack bestWand = InventoryUtil.streamInventory(player.getInventory())
						.filter(x -> x.getItem() instanceof WandItem)
						.max(Comparator.comparingInt(x -> WandItem.aspectsFrom(x).get(Aspects.ORDER)))
						.orElse(null);
				if(bestWand != null && WandItem.aspectsFrom(bestWand).get(Aspects.ORDER) > 0){
					WandItem.updateAspects(bestWand, x -> x.take(Aspects.ORDER, 1));
					rechargeTimer += 6;
				}
			}
			if(rechargeTimer > 0 && player.world.getTime() % 2 == 0 && InventoryUtil.hasTrinket(player, ArcanaRegistry.RING_OF_TWIN_HEARTBEATS))
				rechargeTimer += 1;
			if(rechargeTimer >= MAX_RECHARGE_TIMER){
				halfPoints++;
				rechargeTimer = 0;
				lastRechargeTime = player.world.getTime();
				sync();
			}
		}
	}
	
	public void sync(){
		player.syncComponent(KEY);
	}
	
	public void readFromNbt(@NotNull NbtCompound compound){
		halfPoints = compound.getInt("points");
		rechargeTimer = compound.getInt("rechargeTimer");
		lastRechargeTime = compound.getLong("lastRechargeTime");
		lastActivateTime = compound.getLong("lastActivateTime");
	}
	
	public void writeToNbt(@NotNull NbtCompound compound){
		compound.putInt("points", halfPoints);
		compound.putInt("rechargeTimer", rechargeTimer);
		compound.putLong("lastRechargeTime", lastRechargeTime);
		compound.putLong("lastActivateTime", lastActivateTime);
	}
}