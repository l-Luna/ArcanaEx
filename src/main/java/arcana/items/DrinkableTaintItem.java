package arcana.items;

import arcana.legacy_components.Researcher;
import arcana.research.BuiltinResearch;
import arcana.research.Research;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class DrinkableTaintItem extends Item{
	
	public DrinkableTaintItem(Settings settings){
		super(settings);
	}
	
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user){
		ItemStack ret = super.finishUsing(stack, world, user);
		
		if(user instanceof PlayerEntity pe){
			Researcher r = Researcher.from(pe);
			r.completePuzzle(Research.getPuzzle(BuiltinResearch.drinkableTaintPuzzle));
			r.doSync();
		}
		
		return ret;
	}
}