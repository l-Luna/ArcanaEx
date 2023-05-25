package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MysticMistBlockEntity extends BlockEntity{
	
	public MysticMistBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.MYSTIC_MIST_BE, pos, state);
	}
	
	/*
	theoretically more complicated, but let's stick to Just Water for now
	- find Relevant Blocks (crops, fire, empty crucibles)
	- draw clouds over them
	- do Effects
	*/
	
	public static void tick(World world, BlockPos pos, BlockState state, MysticMistBlockEntity mist){
	
	}
}
