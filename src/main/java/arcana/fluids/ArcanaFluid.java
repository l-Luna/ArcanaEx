package arcana.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.StateManager;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;

import java.util.Optional;

public abstract class ArcanaFluid extends FlowableFluid{
	
	private final boolean isStillVariant;
	
	public ArcanaFluid(boolean isStillVariant){
		this.isStillVariant = isStillVariant;
	}
	
	// custom fluid properties
	
	public abstract TagKey<Fluid> getTag();
	
	public abstract int getMaxLevel();
	
	public abstract String getTexturePath();
	
	public Optional<BlockState> interact(FluidState self, FluidState other){
		return Optional.empty();
	}
	
	public abstract float getEntityPushStrength(Entity entity);
	public abstract float getEntityDragFactor(Entity entity);
	public abstract float getEntitySpeedFactor(Entity entity);
	public void onEntityInteractTick(Entity entity){}
	
	//
	
	// see FluidBlockMixin for other fluids flowing into this fluid
	protected void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState){
		FluidState belowState = world.getFluidState(pos.down());
		Optional<BlockState> interaction = interact(fluidState, belowState);
		if(interaction.isPresent()){
			world.setBlockState(pos, interaction.get(), Block.NOTIFY_ALL);
			world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
			return;
		}
		
		super.flow(world, pos, state, direction, fluidState);
	}
	
	// generic impls
	
	public boolean matchesType(Fluid fluid){
		return fluid == getFlowing() || fluid == getStill();
	}
	
	protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction){
		return false;
	}
	
	protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state){
		BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
		Block.dropStacks(state, world, pos, blockEntity);
	}
	
	protected float getBlastResistance(){
		return 100;
	}
	
	// replace separate subclasses with a boolean
	
	protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder){
		super.appendProperties(builder);
		if(!isStillVariant)
			builder.add(LEVEL);
	}
	
	public boolean isStill(){
		return isStillVariant;
	}
	
	public boolean isStill(FluidState state){
		return isStillVariant;
	}
	
	public int getLevel(FluidState state){
		return isStillVariant ? getMaxLevel() : state.get(LEVEL);
	}
}