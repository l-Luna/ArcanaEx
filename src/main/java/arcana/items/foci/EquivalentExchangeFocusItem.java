package arcana.items.foci;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.items.FocusItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class EquivalentExchangeFocusItem extends FocusItem{
	
	private static final Map<TagKey<Block>, Item> MINEABLE_TAGS = Map.of(
			BlockTags.AXE_MINEABLE, Items.DIAMOND_AXE,
			BlockTags.HOE_MINEABLE, Items.DIAMOND_HOE,
			BlockTags.PICKAXE_MINEABLE, Items.DIAMOND_PICKAXE,
			BlockTags.SHOVEL_MINEABLE, Items.DIAMOND_SHOVEL,
			BlockTags.SWORD_EFFICIENT, Items.DIAMOND_SWORD
	);
	
	private static final List<TagKey<Block>> MINING_LEVEL_TAGS = List.of(
			BlockTags.INCORRECT_FOR_WOODEN_TOOL,
			BlockTags.INCORRECT_FOR_STONE_TOOL,
			BlockTags.INCORRECT_FOR_IRON_TOOL,
			BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
			BlockTags.INCORRECT_FOR_NETHERITE_TOOL
	);
	
	public EquivalentExchangeFocusItem(Settings settings){
		super(settings);
	}
	
	public AspectMap deciCastCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		int amount = 1;
		// TODO: move cost calculation to casting code
		if(user != null && user.getWorld() != null){
			// (0.7 order, 0.7 entropy) * mining level + (0.1, 0.1)
			// "requires a tool" increases the mining level to 1
			BlockPos pos = ((BlockHitResult)user.raycast(5.5, 0, false)).getBlockPos();
			BlockState looking = user.getWorld().getBlockState(pos);
			amount = Math.max(looking.isToolRequired() ? 1 : 0, getRequiredMiningLevel(looking) + 1) * 7 + 1;
			// for display purposes
			if(looking.getHardness(user.getWorld(), pos) == -1)
				amount = 100000;
		}
		return AspectMap.fromAspectStacks(new AspectStack(Aspects.ORDER, amount), new AspectStack(Aspects.ENTROPY, amount));
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
				if(old.getHardness(world, pos) != -1 && !toPlace.equals(old)){
					if(ctx.getWorld() instanceof ServerWorld){
						boolean changed = false;
						for(ItemStack stack : old.getDroppedStacks(swapContext(ctx, old)))
							changed |= (player.giveItemStack(stack));
						if(changed){
							player.currentScreenHandler.sendContentUpdates();
							player.getInventory().markDirty();
						}
					}
					world.setBlockState(pos, toPlace);
					otherStack.decrement(1);
					return ActionResult.SUCCESS;
				}
			}
		}
		return super.useOnBlock(ctx);
	}
	
	private static LootContextParameterSet.Builder swapContext(ItemUsageContext ctx, BlockState target){
		return new LootContextParameterSet.Builder((ServerWorld)ctx.getWorld())
				.add(LootContextParameters.ORIGIN, ctx.getHitPos())
				.add(LootContextParameters.TOOL, toolFor(target, ctx.getWorld()))
				.add(LootContextParameters.THIS_ENTITY, ctx.getPlayer())
				.add(LootContextParameters.BLOCK_STATE, target)
				.addOptional(LootContextParameters.BLOCK_ENTITY, ctx.getWorld().getBlockEntity(ctx.getBlockPos()));
	}
	
	// perform swaps with diamond tools
	private static ItemStack toolFor(BlockState target, World world){
		for(var entry : MINEABLE_TAGS.entrySet())
			if(target.isIn(entry.getKey())){
				ItemStack stack = new ItemStack(entry.getValue());
				stack.addEnchantment(world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SILK_TOUCH).get(), 1);
				return stack;
			}
		return ItemStack.EMPTY;
	}
	
	private static int getRequiredMiningLevel(BlockState block){
		for(int i = 0; i < MINING_LEVEL_TAGS.size(); i++)
			if(!block.isIn(MINING_LEVEL_TAGS.get(i)))
				return i;
		return MINING_LEVEL_TAGS.size() + 1;
	}
}