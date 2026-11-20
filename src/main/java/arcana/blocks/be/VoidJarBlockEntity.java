package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectStack;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class VoidJarBlockEntity extends WardedJarBlockEntity{
	
	public VoidJarBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.VOID_JAR_BE, pos, state);
	}
	
	public AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from){
		AspectStack remainder = super.accept(stack, world, pos, from);
		// TODO: release flux
		return null;
	}
}