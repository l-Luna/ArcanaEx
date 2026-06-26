package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.items.components.ArcanaItemComponentTypes;
import arcana.items.components.FragileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity{
	
	@Shadow
	public abstract ItemStack getStack();
	
	public ItemEntityMixin(EntityType<?> type, World world){
		super(type, world);
	}
	
	@Inject(method = "setStack", at = @At("TAIL"))
	void applyItemModifiers(ItemStack stack, CallbackInfo ci){
		if(stack.isIn(ArcanaTags.FLOATS))
			setNoGravity(true);
	}
	
	@Inject(method = "tick", at = @At("TAIL"))
	void applyItemPhysics(CallbackInfo ci){
		ItemStack stack = getStack();
		if(stack.isIn(ArcanaTags.FLOATS) && getVelocity().horizontalLengthSquared() > 1.0E-5F)
			setVelocity(getVelocity().multiply(0.9f, 0.9f, 0.9f));
		World world = getWorld();
		if(world.isClient && world.random.nextInt(12) == 0 && stack.isOf(ArcanaRegistry.WISPY_ESSENCE))
			world.addParticle(ArcanaRegistry.LIGHTNING,
					false,
					getX() + world.random.nextGaussian() / 9,
					getY() + world.random.nextGaussian() / 9,
					getZ() + world.random.nextGaussian() / 9,
					0,
					0.02,
					0);
		FragileComponent c = stack.get(ArcanaItemComponentTypes.FRAGILE);
		if(!world.isClient && c != null && (horizontalCollision || verticalCollision)){
			world.syncWorldEvent(WorldEvents.INSTANT_SPLASH_POTION_SPLASHED, getBlockPos(), c.colour());
			if(c.effect().isPresent())
				// see PotionEntity
				for(LivingEntity entity : world.getNonSpectatingEntities(LivingEntity.class, getBoundingBox().expand(4, 2, 4)))
					if(entity.isAffectedBySplashPotions())
						c.effect().get().value().applyInstantEffect(this, null, entity, 0, 1);
			discard();
		}
	}
}