package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CrucibleBlock extends BlockWithEntity{
	
	protected static final VoxelShape INSIDE = createCuboidShape(2, 4, 2, 14, 15, 14);
	protected static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(
			createCuboidShape(0, 0, 0, 16, 15, 16),
			VoxelShapes.union(
					createCuboidShape(0, 0, 3, 16, 3, 13),
					createCuboidShape(3, 0, 0, 13, 3, 16),
					createCuboidShape(2, 0, 2, 14, 3, 14),
					INSIDE),
			BooleanBiFunction.ONLY_FIRST);
	
	public static final BooleanProperty FULL = BooleanProperty.of("full");
	
	public CrucibleBlock(Settings settings){
		super(settings);
		setDefaultState(stateManager.getDefaultState().with(FULL, false));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		builder.add(FULL);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return SHAPE;
	}
	
	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos){
		return INSIDE;
	}
	
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type){
		return false;
	}
	
	@Nullable
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new CrucibleBlockEntity(pos, state);
	}
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World _world, BlockState _state, BlockEntityType<T> type){
		return checkType(type, ArcanaRegistry.CRUCIBLE_BE, (world, pos, state, entity) -> entity.tick());
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
		ItemStack itemstack = player.getStackInHand(hand);
		if(itemstack.isEmpty()){
			if(player.isSneaking()){
				if(state.get(FULL)){
					if(!world.isClient){
						world.setBlockState(pos, state.with(FULL, false), 2);
						world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1, 1);
					}
					((CrucibleBlockEntity)world.getBlockEntity(pos)).setEmpty();
				}
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		}else{
			Item item = itemstack.getItem();
			if(item == Items.WATER_BUCKET){
				if(!state.get(FULL) && !world.isClient){
					if(!player.isCreative())
						player.setStackInHand(hand, new ItemStack(Items.BUCKET));
					player.incrementStat(Stats.FILL_CAULDRON);
					world.setBlockState(pos, state.with(FULL, true), 2);
					world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1, 1);
				}
				return ActionResult.SUCCESS;
			}else if(item == Items.BUCKET){
				if(state.get(FULL) && !world.isClient && ((CrucibleBlockEntity)world.getBlockEntity(pos)).getAspects().isEmpty()){
					if(!player.isCreative()){
						itemstack.decrement(1);
						if(itemstack.isEmpty())
							player.setStackInHand(hand, new ItemStack(Items.WATER_BUCKET));
						else if(!player.getInventory().insertStack(new ItemStack(Items.WATER_BUCKET)))
							player.dropItem(new ItemStack(Items.WATER_BUCKET), false);
					}
					player.incrementStat(Stats.USE_CAULDRON);
					world.setBlockState(pos, state.with(FULL, false), 2);
					world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1, 1);
				}
				return ActionResult.SUCCESS;
			}
		}
		return super.onUse(state, world, pos, player, hand, hit);
	}
	
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random rand){
		if(state.get(FULL) && ((CrucibleBlockEntity)world.getBlockEntity(pos)).isBoiling()){
			double x = pos.getX();
			double y = pos.getY();
			double z = pos.getZ();
			// TODO: custom particles?
			world.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x + .125 + rand.nextFloat() * .75f, y + .8125f, z + .125 + rand.nextFloat() * .75f, 0, .04, 0);
			world.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x + .125 + rand.nextFloat() * .75f, y + .8125f, z + .125 + rand.nextFloat() * .75f, 0, .04, 0);
		}
	}
}