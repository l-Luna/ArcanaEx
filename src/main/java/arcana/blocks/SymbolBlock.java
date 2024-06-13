package arcana.blocks;

import arcana.entities.locomotive.Symbol;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class SymbolBlock extends Block{
	
	protected static final VoxelShape shape = createCuboidShape(3, 0, 3, 13, 1, 13);
	
	private final Symbol symbol;
	
	public SymbolBlock(Settings settings, Symbol symbol){
		super(settings);
		this.symbol = symbol;
	}
	
	public Symbol getSymbol(){
		return symbol;
	}
	
	public MutableText getName(){
		return Text.translatable("block.arcana.symbol", Text.translatable("arcana.symbol." + symbol.name()));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(Properties.HORIZONTAL_FACING);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing());
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return shape;
	}
	
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return VoxelShapes.empty();
	}
	
	public static class AsItem extends BlockItem{
		
		public AsItem(Block block, Settings settings){
			super(block, settings);
		}
		
		public Text getName(ItemStack stack){
			return getBlock().getName();
		}
	}
}