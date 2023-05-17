package arcana.items.foci;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.items.FocusItem;
import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class EquivalentExchangeFocusItem extends FocusItem{
	
	private static final Map<TagKey<Block>, Item> mineableTags = Map.of(
			BlockTags.AXE_MINEABLE, Items.DIAMOND_AXE,
			BlockTags.HOE_MINEABLE, Items.DIAMOND_HOE,
			BlockTags.PICKAXE_MINEABLE, Items.DIAMOND_PICKAXE,
			BlockTags.SHOVEL_MINEABLE, Items.DIAMOND_SHOVEL,
			FabricMineableTags.SWORD_MINEABLE, Items.DIAMOND_SWORD,
			FabricMineableTags.SHEARS_MINEABLE, Items.SHEARS
	);
	
	public EquivalentExchangeFocusItem(Settings settings){
		super(settings);
	}
	
	public AspectMap castCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		// (1 order, 1 entropy) * mining level, min 1
		BlockState looking = user.world.getBlockState(((BlockHitResult)user.raycast(5.5, 0, false)).getBlockPos());
		int amount = Math.max(1, MiningLevelManager.getRequiredMiningLevel(looking));
		return AspectMap.fromAspectStacks(List.of(new AspectStack(Aspects.ORDER, amount), new AspectStack(Aspects.ENTROPY, amount)));
	}
	
	public ActionResult castOnBlock(ItemUsageContext ctx){
		PlayerEntity player = ctx.getPlayer();
		BlockPos pos = ctx.getBlockPos();
		World world = ctx.getWorld();
		Hand hand = ctx.getHand();
		Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
		ItemStack otherStack = player.getStackInHand(otherHand);
		if(!otherStack.isEmpty() && otherStack.getItem() instanceof BlockItem bi){
			// swap out the stack with `otherStack`
			BlockState toPlace = bi.getPlacementState(new ItemPlacementContext(player, hand, otherStack, ctx.getHitResult()));
			if(toPlace == null || !toPlace.canPlaceAt(world, pos))
				toPlace = bi.getBlock().getDefaultState();
			
			if(toPlace.canPlaceAt(world, pos)){
				BlockState old = world.getBlockState(pos);
				if(ctx.getWorld() instanceof ServerWorld)
					old.getDroppedStacks(swapContext(ctx, old)).forEach(player::giveItemStack);
				world.setBlockState(pos, toPlace);
				otherStack.decrement(1);
				return ActionResult.SUCCESS;
			}
		}
		return super.useOnBlock(ctx);
	}
	
	private static LootContext.Builder swapContext(ItemUsageContext ctx, BlockState target){
		return new LootContext.Builder((ServerWorld)ctx.getWorld())
				.random(ctx.getWorld().random)
				.parameter(LootContextParameters.ORIGIN, ctx.getHitPos())
				.parameter(LootContextParameters.TOOL, toolFor(target))
				.parameter(LootContextParameters.THIS_ENTITY, ctx.getPlayer())
				.parameter(LootContextParameters.BLOCK_STATE, target)
				.optionalParameter(LootContextParameters.BLOCK_ENTITY, ctx.getWorld().getBlockEntity(ctx.getBlockPos()));
	}
	
	// perform swaps with diamond tools
	private static ItemStack toolFor(BlockState target){
		for(var entry : mineableTags.entrySet())
			if(target.isIn(entry.getKey())){
				var stack = new ItemStack(entry.getValue());
				stack.addEnchantment(Enchantments.SILK_TOUCH, 1);
				return stack;
			}
		return ItemStack.EMPTY;
	}
}