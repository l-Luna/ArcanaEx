package arcana.items;

import arcana.entities.ThrownTaintBottleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class TaintInABottleItem extends Item{
	
	public TaintInABottleItem(Settings settings){
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		ItemStack itemStack = user.getStackInHand(hand);
		world.playSound(
				null,
				user.getX(),
				user.getY(),
				user.getZ(),
				SoundEvents.ENTITY_SPLASH_POTION_THROW,
				SoundCategory.PLAYERS,
				0.5f,
				0.4f / (world.getRandom().nextFloat() * 0.4F + 0.8F)
		);
		if(!world.isClient){
			ThrownTaintBottleEntity bottleEntity = new ThrownTaintBottleEntity(user, world);
			bottleEntity.setItem(itemStack);
			bottleEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0, 1.5f, 1);
			world.spawnEntity(bottleEntity);
		}
		
		user.incrementStat(Stats.USED.getOrCreateStat(this));
		if(!user.getAbilities().creativeMode)
			itemStack.decrement(1);
		
		return TypedActionResult.success(itemStack, world.isClient());
	}
}