package arcana.blocks;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class LightFocusBlock extends WaterloggableBlock{
	
	protected static final VoxelShape shape = createCuboidShape(5, 5, 5, 11, 11, 11);
	public static final IntProperty life = IntProperty.of("life", 0, 8);
	
	public LightFocusBlock(Settings settings){
		super(settings);
		setDefaultState(getDefaultState().with(life, 8));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(life);
	}
	
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		if(random.nextInt(3) == 0){
			var curLife = state.get(life);
			if(curLife == 0)
				world.setBlockState(pos, (state.get(waterlogged) ? Blocks.WATER : Blocks.AIR).getDefaultState());
			else
				world.setBlockState(pos, state.with(life, curLife - 1));
		}
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return shape;
	}
	
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return VoxelShapes.empty();
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.INVISIBLE;
	}
	
	public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos){
		return true;
	}
	
	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos){
		return 1;
	}
}
