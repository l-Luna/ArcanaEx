package arcana.mixin;

import arcana.blocks.WardedCampfireBlock;
import arcana.cca_components.RunicShielding;
import arcana.items.BootsOfTheTravellerItem;
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
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
}