package arcana.items;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class DrinkItem extends Item{
	
	public DrinkItem(Settings settings){
		super(settings);
	}
	
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user){
		super.finishUsing(stack, world, user);
		if(user instanceof ServerPlayerEntity player){
			Criteria.CONSUME_ITEM.trigger(player, stack);
			player.incrementStat(Stats.USED.getOrCreateStat(this));
		}
		
		if(stack.isEmpty())
			return new ItemStack(Items.GLASS_BOTTLE);
		else{
			if(user instanceof PlayerEntity player && !player.getAbilities().creativeMode){
				ItemStack itemStack = new ItemStack(Items.GLASS_BOTTLE);
				if(!player.getInventory().insertStack(itemStack))
					player.dropItem(itemStack, false);
			}
			
			return stack;
		}
	}
	
	public int getMaxUseTime(ItemStack stack){
		return 28;
	}
	
	public UseAction getUseAction(ItemStack stack){
		return UseAction.DRINK;
	}
	
	public SoundEvent getEatSound(){
		return SoundEvents.ENTITY_GENERIC_DRINK;
	}
}