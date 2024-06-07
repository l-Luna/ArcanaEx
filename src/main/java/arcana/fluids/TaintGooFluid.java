package arcana.fluids;

import arcana.ArcanaRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class TaintGooFluid extends FlowableFluid{
	
	public Fluid getFlowing(){
		return ArcanaRegistry.FLOWING_TAINT_GOO;
	}
	
	public Fluid getStill(){
		return ArcanaRegistry.STILL_TAINT_GOO;
	}
	
	public boolean matchesType(Fluid fluid){
		return fluid == getFlowing() || fluid == getStill();
	}
	
	protected boolean isInfinite(){
		return false;
	}
	
	protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state){
		BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
		Block.dropStacks(state, world, pos, blockEntity);
	}
	
	protected int getFlowSpeed(WorldView world){
		return 2;
	}
	
	protected int getLevelDecreasePerBlock(WorldView world){
		return 1;
	}
	
	public Item getBucketItem(){
		return ArcanaRegistry.TAINT_GOO_BUCKET;
	}
	
	protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction){
		return false;
	}
	
	public int getTickRate(WorldView world){
		return 40;
	}
	
	protected float getBlastResistance(){
		return 100;
	}
	
	protected BlockState toBlockState(FluidState state){
		return ArcanaRegistry.TAINT_GOO.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
	}
	
	public static class Flowing extends TaintGooFluid{
		
		protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
			super.appendProperties(builder);
			builder.add(LEVEL);
		}
		
		public int getLevel(FluidState state) {
			return state.get(LEVEL);
		}
		
		public boolean isStill(FluidState state){
			return false;
		}
	}
	
	public static class Still extends TaintGooFluid{
		
		public int getLevel(FluidState state){
			return 4;
		}
		
		public boolean isStill(FluidState state){
			return true;
		}
	}
}