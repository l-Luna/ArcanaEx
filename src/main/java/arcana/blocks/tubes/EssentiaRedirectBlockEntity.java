package arcana.blocks.tubes;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectSpeck;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class EssentiaRedirectBlockEntity extends EssentiaTubeBlockEntity{
	
	public EssentiaRedirectBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state){
		super(type, pos, state);
	}
	
	public EssentiaRedirectBlockEntity(BlockPos pos, BlockState state){
		this(ArcanaRegistry.ESSENTIA_REDIRECT_BE, pos, state);
	}
	
	public void insert(AspectSpeck speck){
		super.insert(speck);
		// if we can send the speck in the right direction, do it
		Direction towards = getCachedState().get(EssentiaRedirectBlock.FACING);
		if(EssentiaTubeBlock.connectsTo(world.getBlockState(pos.offset(towards)).getBlock()))
			speck.direction = towards;
	}
}