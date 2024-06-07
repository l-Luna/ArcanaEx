package arcana.blocks.tubes;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectSpeck;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Collection;

public class EssentiaRouterBlockEntity extends EssentiaTubeBlockEntity{
	
	public EssentiaRouterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state){
		super(type, pos, state);
	}
	
	public EssentiaRouterBlockEntity(BlockPos pos, BlockState state){
		this(ArcanaRegistry.ESSENTIA_ROUTER_BE, pos, state);
	}
	
	public void insert(AspectSpeck speck){
		super.insert(speck);
		// shuffle the speck's direction, including upwards
		Collection<Direction> u = Direction.shuffle(world.random);
		// don't allow backflow
		u.remove(speck.direction.getOpposite());
		for(Direction direction : u)
			if(EssentiaTubeBlock.connectsTo(world.getBlockState(pos.offset(direction)).getBlock())){
				speck.direction = direction;
				break;
			}
	}
}