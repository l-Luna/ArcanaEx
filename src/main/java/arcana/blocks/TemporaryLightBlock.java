package arcana.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class TemporaryLightBlock extends WaterloggableBlock{
	
	private static final MapCodec<TemporaryLightBlock> CODEC = createCodec(TemporaryLightBlock::new);
	
	protected static final VoxelShape SHAPE = createCuboidShape(5, 5, 5, 11, 11, 11);
	public static final IntProperty LIFE = IntProperty.of("life", 0, 8);
	
	public TemporaryLightBlock(Settings settings){
		super(settings);
		setDefaultState(getDefaultState().with(LIFE, 8));
	}
	
	protected MapCodec<? extends Block> getCodec(){
		return CODEC;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(LIFE);
	}
	
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		if(random.nextInt(3) == 0){
			var curLife = state.get(LIFE);
			if(curLife == 0)
				world.setBlockState(pos, (state.get(WATERLOGGED) ? Blocks.WATER : Blocks.AIR).getDefaultState());
			else
				world.setBlockState(pos, state.with(LIFE, curLife - 1));
		}
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return SHAPE;
	}
	
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return VoxelShapes.empty();
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.INVISIBLE;
	}
	
	protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
		return stateFrom.isOf(this) || super.isSideInvisible(state, stateFrom, direction);
	}
	
	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos){
		return 1;
	}
	
	public boolean canReplace(BlockState state, ItemPlacementContext context){
		return true;
	}
}
