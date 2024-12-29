package arcana.blocks;

import com.unascribed.lib39.weld.api.BigBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WoodenStatueBlock extends BigBlock{
	
	public enum Type{
		speak,
		see,
		hear
	}
	
	public static final IntProperty y = IntProperty.of("y", 0, 1);
	public static final DirectionProperty facing = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty waterlogged = Properties.WATERLOGGED;
	
	protected static final VoxelShape topShape = Block.createCuboidShape(1, 0, 1, 15, 7, 15);
	protected static final VoxelShape bottomShape = Block.createCuboidShape(1, 0, 1, 15, 16, 15);
	
	public final Type type;
	
	public WoodenStatueBlock(Settings settings, Type type){
		super(null, y, null, settings);
		this.type = type;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(y, facing, waterlogged);
	}
	
	public MutableText getName(){
		return Text.translatable("block.arcana.wooden_statue");
	}
	
	public String getTranslationKey(){
		return "block.arcana.wooden_statue";
	}
	
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options){
		super.appendTooltip(stack, world, tooltip, options);
		tooltip.add(Text.translatable("block.arcana." + type.name() + "_no_evil").formatted(Formatting.GRAY));
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		if(state.get(y) == 1)
			return topShape;
		return bottomShape;
	}
	
	public FluidState getFluidState(BlockState state){
		return state.get(waterlogged) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		return super.getPlacementState(ctx)
				.with(waterlogged, fluidState.getFluid() == Fluids.WATER)
				.with(facing, ctx.getPlayerFacing().getOpposite());
	}
}