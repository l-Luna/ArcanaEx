package arcana.items;

import arcana.aspects.Aspect;
import arcana.blocks.CrystalClusterBlock;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ClusterSeedItem extends Item{
	
	// can't use BlockItem because that replaces the normal BlockItem
	private final Aspect aspect;
	private final Block block;
	
	public ClusterSeedItem(Block block, Settings settings, Aspect aspect){
		super(settings);
		this.block = block;
		this.aspect = aspect;
	}
	
	public ActionResult useOnBlock(ItemUsageContext context){
		ActionResult result = place(new ItemPlacementContext(context));
		if(!result.isAccepted() && isFood()){
			ActionResult actual = use(context.getWorld(), context.getPlayer(), context.getHand()).getResult();
			return actual == ActionResult.CONSUME ? ActionResult.CONSUME_PARTIAL : actual;
		}
		return result;
	}
	
	public ActionResult place(ItemPlacementContext context){
		if(!context.canPlace())
			return ActionResult.FAIL;
		else{
			BlockState state = getPlacementState(context);
			if(state == null)
				return ActionResult.FAIL;
			else if(!place(context, state))
				return ActionResult.FAIL;
			else{
				BlockPos pos = context.getBlockPos();
				World world = context.getWorld();
				PlayerEntity player = context.getPlayer();
				ItemStack stack = context.getStack();
				BlockState actual = world.getBlockState(pos);
				if(actual.isOf(state.getBlock())){
					actual.getBlock().onPlaced(world, pos, actual, player, stack);
					if(player instanceof ServerPlayerEntity spe)
						Criteria.PLACED_BLOCK.trigger(spe, pos, stack);
				}
				
				BlockSoundGroup sounds = actual.getSoundGroup();
				world.playSound(player, pos, getPlaceSound(actual), SoundCategory.BLOCKS, (sounds.getVolume() + 1) / 2f, sounds.getPitch() * .8f);
				world.emitGameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Emitter.of(player, actual));
				if(player == null || !player.getAbilities().creativeMode)
					stack.decrement(1);
				
				return ActionResult.success(world.isClient);
			}
		}
	}
	
	protected SoundEvent getPlaceSound(BlockState state) {
		return state.getSoundGroup().getPlaceSound();
	}
	
	protected BlockState getPlacementState(ItemPlacementContext context){
		BlockState state = block.getPlacementState(context);
		return state != null && canPlace(context, state) ? state.with(CrystalClusterBlock.size, 0) : null;
	}
	
	protected boolean canPlace(ItemPlacementContext cx, BlockState state){
		PlayerEntity playerEntity = cx.getPlayer();
		ShapeContext shapeContext = playerEntity == null ? ShapeContext.absent() : ShapeContext.of(playerEntity);
		return state.canPlaceAt(cx.getWorld(), cx.getBlockPos()) && cx.getWorld().canPlace(state, cx.getBlockPos(), shapeContext);
	}
	
	protected boolean place(ItemPlacementContext context, BlockState state){
		return context.getWorld().setBlockState(context.getBlockPos(), state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
	}
	
	public Text getName(ItemStack stack){
		return getName();
	}
	
	public Text getName(){
		return Text.translatable("item.arcana.cluster_seed", aspect.name());
	}
}