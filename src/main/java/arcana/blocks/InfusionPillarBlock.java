package arcana.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class InfusionPillarBlock extends BlockWithEntity{
	
	public static final Property<Direction> facing = Properties.HORIZONTAL_FACING;
	
	public InfusionPillarBlock(Settings settings){
		super(settings);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(facing);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(facing, ctx.getPlayerFacing().getOpposite());
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new InfusionPillarBlockEntity(pos, state);
	}
}