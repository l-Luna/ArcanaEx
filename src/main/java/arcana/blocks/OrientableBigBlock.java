package arcana.blocks;

import com.unascribed.lib39.weld.api.BigBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class OrientableBigBlock extends BigBlock{
	
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	
	@Nullable
	private final IntProperty right, up, fwd;
	
	// replaceable with flexible constructor bodies in future jva
	private static IntProperty stashedRight, stashedUp, stashedFwd;
	
	public static OrientableBigBlock create(@Nullable IntProperty right, @Nullable IntProperty up, @Nullable IntProperty fwd, Settings settings){
		stashedRight = right;
		stashedUp = up;
		stashedFwd = fwd;
		return new OrientableBigBlock(right, up, fwd, settings);
	}
	
	protected OrientableBigBlock(@Nullable IntProperty right, @Nullable IntProperty up, @Nullable IntProperty fwd, Settings settings){
		super(right, up, fwd, settings);
		this.right = right;
		this.up = up;
		this.fwd = fwd;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(FACING);
		if(stashedRight != null)
			builder.add(stashedRight);
		if(stashedUp != null)
			builder.add(stashedUp);
		if(stashedFwd != null)
			builder.add(stashedFwd);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		return super.getPlacementState(ctx).with(FACING, ctx.getPlayerFacing());
	}
	
	public int getX(BlockState state){
		return switch(state.get(FACING)){
			case NORTH -> super.getX(state);
			case SOUTH -> super.getXSize(state) - super.getX(state) - 1;
			case WEST -> super.getZ(state);
			case EAST -> super.getZSize(state) - super.getZ(state) - 1;
			case UP, DOWN -> throw new IllegalStateException();
		};
	}
	
	public BlockState setX(BlockState state, int x){
		return switch(state.get(FACING)){
			case NORTH -> super.setX(state, x);
			case SOUTH -> super.setX(state, super.getXSize(state) - x - 1);
			case WEST -> super.setZ(state, x);
			case EAST -> super.setZ(state, super.getZSize(state) - x - 1);
			case UP, DOWN -> throw new IllegalStateException();
		};
	}
	
	public int getXSize(BlockState state){
		return state.get(FACING).getAxis() == Direction.Axis.X ? super.getZSize(state) : super.getXSize(state);
	}
	
	public int getZ(BlockState state){
		return switch(state.get(FACING)){
			case NORTH -> super.getZ(state);
			case SOUTH -> super.getZSize(state) - super.getZ(state) - 1;
			case EAST -> super.getX(state);
			case WEST -> super.getXSize(state) - super.getX(state) - 1;
			case UP, DOWN -> throw new IllegalStateException();
		};
	}
	
	public BlockState setZ(BlockState state, int z){
		return switch(state.get(FACING)){
			case NORTH -> super.setZ(state, z);
			case SOUTH -> super.setZ(state, super.getZSize(state) - z - 1);
			case EAST -> super.setX(state, z);
			case WEST -> super.setX(state, super.getXSize(state) - z - 1);
			case UP, DOWN -> throw new IllegalStateException();
		};
	}
	
	public int getZSize(BlockState state){
		return state.get(FACING).getAxis() == Direction.Axis.X ? super.getXSize(state) : super.getZSize(state);
	}
}