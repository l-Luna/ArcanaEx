package arcana.client;

import arcana.items.TomeOfSharingItem;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TomeOfSharingPredicateProvider implements UnclampedModelPredicateProvider{
	
	public float unclampedCall(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed){
		var stages = TomeOfSharingItem.getBoundResearch(stack);
		var puzzles = TomeOfSharingItem.getBoundPuzzles(stack);
		return stages.isEmpty() && puzzles.isEmpty() ? 0 : 1;
	}
}
