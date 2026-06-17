package arcana.blocks.deco;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unascribed.lib39.weld.api.BigBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import java.util.List;
import java.util.Locale;

public class WoodenStatueBlock extends BigBlock{
	
	public enum Type{
		SPEAK,
		SEE,
		HEAR
	}
	
	private static final MapCodec<WoodenStatueBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			createSettingsCodec(),
			Codec.STRING.xmap(Type::valueOf, Enum::name).fieldOf("type").forGetter(x -> x.type)
	).apply(i, WoodenStatueBlock::new));
	
	public static final IntProperty Y = IntProperty.of("y", 0, 1);
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	
	protected static final VoxelShape TOP_SHAPE = Block.createCuboidShape(1, 0, 1, 15, 7, 15);
	protected static final VoxelShape BOTTOM_SHAPE = Block.createCuboidShape(1, 0, 1, 15, 16, 15);
	
	public final Type type;
	
	public WoodenStatueBlock(Settings settings, Type type){
		super(null, Y, null, settings);
		this.type = type;
	}
	
	protected MapCodec<? extends Block> getCodec(){
		return CODEC;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(Y, FACING, WATERLOGGED);
	}
	
	public MutableText getName(){
		return Text.translatable("block.arcana.wooden_statue");
	}
	
	public String getTranslationKey(){
		return "block.arcana.wooden_statue";
	}
	
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options){
		super.appendTooltip(stack, context, tooltip, options);
		tooltip.add(Text.translatable("block.arcana." + type.name().toLowerCase(Locale.ROOT) + "_no_evil").formatted(Formatting.GRAY));
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		if(state.get(Y) == 1)
			return TOP_SHAPE;
		return BOTTOM_SHAPE;
	}
	
	public FluidState getFluidState(BlockState state){
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		return super.getPlacementState(ctx)
				.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER)
				.with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}
	
	public BlockState rotate(BlockState state, BlockRotation rotation){
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}
	
	public BlockState mirror(BlockState state, BlockMirror mirror){
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}
}