package arcana.mixin;

import arcana.ArcanaDamageSources;
import arcana.ArcanaSounds;
import arcana.blocks.WardedCampfireBlock;
import arcana.cca_components.RunicShielding;
import arcana.items.BootsOfTheTravellerItem;
import arcana.items.trinkets.ClawTrinketItem;
import arcana.util.InventoryUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity{
	
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world){
		super(entityType, world);
	}
	
	@Shadow public abstract HungerManager getHungerManager();
	
	@Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);
	
	@Inject(method = "eatFood", at = @At("HEAD"))
	private void applyWardedCampfireFoodBonus(World world, ItemStack stack, FoodComponent food, CallbackInfoReturnable<ItemStack> cir){
		if(WardedCampfireBlock.isProtected(this))
			getHungerManager().add(food.nutrition() / 2, food.saturation() / 2);
	}
	
	// TODO: use ServerLivingEntityEvents for better compat
	@WrapMethod(method = "damage")
	private boolean applyDamage(DamageSource source, float amount, Operation<Boolean> original){
		if(source.isIn(DamageTypeTags.IS_FALL)){
			int reduction = effectiveFallDamageReduction();
			if(amount > 0 && amount <= reduction)
				return false;
			amount -= reduction;
		}
		if(RunicShielding.from((PlayerEntity)(Object)this).handleDamage(source, amount))
			return false;
		return original.call(source, amount);
	}
	
	@Unique
	private int effectiveFallDamageReduction(){
		boolean boostFromBoots = getEquippedStack(EquipmentSlot.FEET).getItem() instanceof BootsOfTheTravellerItem;
		return boostFromBoots ? 3 : 0;
	}
	
	@ModifyExpressionValue(method = "attack",
	                       at = @At(value = "INVOKE",
	                                target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D",
	                                ordinal = 0))
	double applyAttackDamage(double original){
		if(getWeaponStack().isEmpty()){
			Optional<Float> unarmedDamage = InventoryUtil.fromFirstTrinket((PlayerEntity)(Object)this, it -> it.getItem() instanceof ClawTrinketItem c ? c.getUnarmedAttackBonus() : null);
			if(unarmedDamage.isPresent())
				return original + unarmedDamage.get();
		}
		
		return original;
	}
	
	@ModifyReturnValue(method = "getHurtSound", at = @At("RETURN"))
	protected SoundEvent getHurtSound(SoundEvent original, DamageSource source){
		if(source.getTypeRegistryEntry().matchesKey(ArcanaDamageSources.PRISMATIC_LIGHT_KEY))
			return ArcanaSounds.HURT_PRISMATIC_LIGHT;
		else if(source.getTypeRegistryEntry().matchesKey(ArcanaDamageSources.FLAME_ORB_KEY))
			return ArcanaSounds.HURT_BURNING_POWERFUL;
		else if(source.getTypeRegistryEntry().matchesKey(ArcanaDamageSources.PUTREFACTION_KEY))
			return ArcanaSounds.HURT_PUTREFACTION;
		return original;
	}
}