package arcana.blocks;

import arcana.api.AspectIo;
import arcana.aspects.AspectStack;
import arcana.blocks.be.WardedJarBlockEntity;
import arcana.blocks.tubes.EssentiaTubeBlock;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WardedJarBlock extends BlockWithEntity implements AspectIo{
	
	private static final MapCodec<WardedJarBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			createSettingsCodec(),
			Codec.BOOL.fieldOf("is_void_jar").forGetter(x -> x.isVoidJar)
	).apply(i, WardedJarBlock::new));
	
	public static final BooleanProperty CONNECTED = BooleanProperty.of("connected");
	public static final VoxelShape SHAPE = createCuboidShape(3, 0, 3, 13, 14, 13);
	
	private final boolean isVoidJar;
	
	public WardedJarBlock(Settings settings, boolean isVoidJar){
		super(settings);
		this.isVoidJar = isVoidJar;
		setDefaultState(getStateManager().getDefaultState().with(CONNECTED, false));
	}
	
	protected MapCodec<? extends BlockWithEntity> getCodec(){
		return CODEC;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(CONNECTED);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return SHAPE;
	}
	
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighbor, WorldAccess world, BlockPos pos, BlockPos neighborPos){
		if(direction == Direction.UP)
			if(neighbor.getBlock() instanceof EssentiaTubeBlock)
				return state.with(CONNECTED, true);
			else
				return state.with(CONNECTED, false);
		else
			return state;
	}
	
	public AspectStack accept(AspectStack speck, World world, BlockPos pos, Direction from){
		return world.getBlockEntity(pos) instanceof WardedJarBlockEntity self ? self.accept(speck, world, pos, from) : speck;
	}
	
	public @Nullable AspectStack draw(int max, World world, BlockPos pos, Direction from){
		return world.getBlockEntity(pos) instanceof WardedJarBlockEntity self ? self.draw(max, world, pos, from) : null;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new WardedJarBlockEntity(pos, state, isVoidJar);
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options){
		super.appendTooltip(stack, context, tooltip, options);
		NbtCompound nbt = stack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
		if(nbt != null && nbt.contains("stored")){
			AspectStack stored = AspectStack.fromNbt(nbt.getCompound("stored"));
			tooltip.add(Text.translatable("tooltip.arcana.wand.focus_cost.individual", stored.amount(), stored.type().name()));
		}
	}
}