package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemConvertible;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BejeweledBeetsBlock extends CropBlock{
	
	private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[]{
			Block.createCuboidShape(0, 0, 0, 16, 2, 16),
			Block.createCuboidShape(0, 0, 0, 16, 4, 16),
			Block.createCuboidShape(0, 0, 0, 16, 6, 16),
			Block.createCuboidShape(0, 0, 0, 16, 8, 16)
	};
	
	public BejeweledBeetsBlock(Settings settings){
		super(settings);
	}
	
	public IntProperty getAgeProperty(){
		return Properties.AGE_3;
	}
	
	public int getMaxAge(){
		return 3;
	}
	
	protected ItemConvertible getSeedsItem(){
		return ArcanaRegistry.BEJEWELED_BEET_SEEDS;
	}
	
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		if(random.nextInt(8) != 0)
			super.randomTick(state, world, pos, random);
	}
	
	protected int getGrowthAmount(World world){
		return super.getGrowthAmount(world) / 3;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		builder.add(Properties.AGE_3);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return AGE_TO_SHAPE[state.get(getAgeProperty())];
	}
}