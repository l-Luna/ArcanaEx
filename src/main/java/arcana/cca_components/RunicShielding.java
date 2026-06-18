package arcana.cca_components;

import arcana.ArcanaRegistry;
import arcana.ArcanaSounds;
import arcana.Registerable;
import arcana.aspects.Aspects;
import arcana.items.WandItem;
import arcana.mixin.accessor.EntityAccessor;
import arcana.mixin.accessor.LivingEntityAccessor;
import arcana.util.InventoryUtil;
import dev.emi.trinkets.api.TrinketsAttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Comparator;

import static arcana.Arcana.arcId;

public class RunicShielding implements Component, AutoSyncedComponent, ServerTickingComponent{
	
	public static final ComponentKey<RunicShielding> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("runic_shielding_user"), RunicShielding.class);
	
	public static final Registerable<EntityAttribute> MAX_SHIELDING = new Registerable<>(new ClampedEntityAttribute("attribute.name.generic.arcana.max_shielding", 0, 0, 100).setTracked(true)).register(Registries.ATTRIBUTE, "max_shielding");
	
	private static final int MAX_RECHARGE_TIMER = 7 * 20;
	private static final Identifier MODIFIER_ID = arcId("trinket_runic_shielding");
	
	public static RunicShielding from(PlayerEntity entity){
		return entity.getComponent(KEY);
	}
	
	public static int getMaxShielding(LivingEntity entity){
		return (int)entity.getAttributeValue(MAX_SHIELDING.entry());
	}
	
	public static AttributeModifiersComponent createAttributeModifiers(float shielding){
		return AttributeModifiersComponent.builder()
				.add(
						RunicShielding.MAX_SHIELDING.entry(),
						new EntityAttributeModifier(MODIFIER_ID, shielding, EntityAttributeModifier.Operation.ADD_VALUE),
						AttributeModifierSlot.MAINHAND
				)
				.build();
	}
	
	public static TrinketsAttributeModifiersComponent createTrinketModifiers(float shielding){
		return TrinketsAttributeModifiersComponent.builder()
				.add(
						RunicShielding.MAX_SHIELDING.entry(),
						new EntityAttributeModifier(MODIFIER_ID, shielding, EntityAttributeModifier.Operation.ADD_VALUE)
				)
				.build();
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
				|| (source.isIn(DamageTypeTags.IS_FALL) && player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))
				|| (player.timeUntilRegen > 10 && amount <= ((LivingEntityAccessor)player).arcana$getLastDamageTaken()))
			return false;
		if(halfPoints <= 1 || source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY))
			return false;
		float frac = MathHelper.clamp(amount / player.getHealth(), 0, 1);
		float chance = MathHelper.sqrt(frac);
		if(player.getRandom().nextFloat() <= chance || player.hasStatusEffect(ArcanaRegistry.WARP_FRAIL.entry())){
			boolean hasHeartTrinket = InventoryUtil.hasTrinket(player, ArcanaRegistry.RING_OF_TWIN_HEARTBEATS);
			halfPoints -= 2;
			rechargeTimer = (hasHeartTrinket ? -17 : -10) * 20;
			lastActivateTime = player.getWorld().getTime();
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
		if(((EntityAccessor)player).arcana$isFirstUpdate())
			return;
		int maxHalfPoints = getMaxShielding(player) * 2;
		if(halfPoints < 0 || halfPoints > maxHalfPoints){
			halfPoints = MathHelper.clamp(halfPoints, 0, maxHalfPoints);
			sync();
		}
		if(maxHalfPoints == 0)
			rechargeTimer = 0;
		if(halfPoints < maxHalfPoints){
			rechargeTimer++;
			if(player.getWorld().getTime() % 6 == 0 && InventoryUtil.hasTrinket(player, ArcanaRegistry.RING_OF_THE_SURGING_BARRIER)){
				ItemStack bestWand = InventoryUtil.streamInventory(player.getInventory())
						.filter(x -> x.getItem() instanceof WandItem)
						.max(Comparator.comparing(x -> WandItem.aspectsFrom(x).get(Aspects.EARTH)))
						.orElse(null);
				if(bestWand != null && WandItem.aspectsFrom(bestWand).get(Aspects.EARTH) > 0){
					WandItem.updateAspects(bestWand, x -> x.take(Aspects.EARTH, 1));
					rechargeTimer += 9;
				}
			}
			if(rechargeTimer > 0 && player.getWorld().getTime() % 2 == 0 && InventoryUtil.hasTrinket(player, ArcanaRegistry.RING_OF_TWIN_HEARTBEATS))
				rechargeTimer += 1;
			if(rechargeTimer >= MAX_RECHARGE_TIMER){
				halfPoints++;
				rechargeTimer = 0;
				lastRechargeTime = player.getWorld().getTime();
				sync();
			}
		}
	}
	
	public void sync(){
		player.syncComponent(KEY);
	}
	
	public void readFromNbt(@NotNull NbtCompound compound, RegistryWrapper.WrapperLookup lookup){
		halfPoints = compound.getInt("points");
		rechargeTimer = compound.getInt("rechargeTimer");
		lastRechargeTime = compound.getLong("lastRechargeTime");
		lastActivateTime = compound.getLong("lastActivateTime");
	}
	
	public void writeToNbt(@NotNull NbtCompound compound, RegistryWrapper.WrapperLookup lookup){
		compound.putInt("points", halfPoints);
		compound.putInt("rechargeTimer", rechargeTimer);
		compound.putLong("lastRechargeTime", lastRechargeTime);
		compound.putLong("lastActivateTime", lastActivateTime);
	}
}