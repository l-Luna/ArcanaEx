package arcana.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class SearchUtil{
	
	public static void randomSearch(World world, BlockPos pos, int radius, int rolls, BiPredicate<BlockPos, BlockState> predicate, BiConsumer<BlockPos, BlockState> then){
		Random rng = world.random;
		for(int i = 0; i < rolls; i++){
			BlockPos there = pos.add(rng.nextBetween(-radius, radius), rng.nextBetween(-radius, radius), rng.nextBetween(-radius, radius));
			BlockState state = world.getBlockState(there);
			if(predicate.test(there, state)){
				then.accept(there, state);
				return;
			}
		}
	}
	
	public static void vRandomSearch(World world, BlockPos pos, int radius, int vspace, int rolls, BiPredicate<BlockPos, BlockState> predicate, BiConsumer<BlockPos, BlockState> then){
		for(int i = 0; i < rolls; i++){
			int x = pos.getX() + world.random.nextBetween(-radius, radius),
				z = pos.getZ() + world.random.nextBetween(-radius, radius);
			for(int yOff = vspace; yOff >= -2; yOff--){
				BlockPos there = new BlockPos(x, pos.getY() + yOff, z);
				BlockState state = world.getBlockState(there);
				if(predicate.test(there, state)){
					then.accept(there, state);
					return;
				}
			}
		}
	}
}