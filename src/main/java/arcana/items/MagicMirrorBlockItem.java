package arcana.items;

import arcana.blocks.be.MagicMirrorBlockEntity;
import arcana.items.components.ArcanaItemComponentTypes;
import arcana.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class MagicMirrorBlockItem extends BlockItem{
	
	public MagicMirrorBlockItem(Block block, Settings settings){
		super(block, settings);
	}
	
	public Optional<TooltipData> getTooltipData(ItemStack stack){
		return Optional.of(new MagicMirrorTooltipData(MagicMirrorBlockItem.getTag(stack)));
	}
	
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
		if(!world.isClient && stack.getOrDefault(ArcanaItemComponentTypes.MAGIC_MIRROR_BUNDLED_FLAG, false)){
			setTag(stack, MathUtil.randomUuid(world.random));
			stack.set(ArcanaItemComponentTypes.MAGIC_MIRROR_BUNDLED_FLAG, false);
			stack.setCount(2);
		}
	}
	
	public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player){
		setTag(stack, MathUtil.randomUuid(world.random));
	}
	
	/*public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks){
		if(isIn(group)){
			ItemStack stack = getDefaultStack();
			stack.getOrCreateNbt().putBoolean("bundled", true);
			stacks.add(stack);
		}
	}*/
	
	protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state){
		if(!world.isClient && world.getBlockEntity(pos) instanceof MagicMirrorBlockEntity mm){
			UUID tag = getTag(stack);
			mm.setTag(tag != null ? tag : MathUtil.randomUuid(world.random));
			return true;
		}
		return super.postPlacement(pos, world, player, stack, state);
	}
	
	public static @Nullable UUID getTag(ItemStack mirrorStack){
		return mirrorStack.getOrDefault(ArcanaItemComponentTypes.MAGIC_MIRROR_TAG, null);
	}
	
	public static ItemStack setTag(ItemStack mirrorStack, UUID uuid){
		mirrorStack.set(ArcanaItemComponentTypes.MAGIC_MIRROR_TAG, uuid);
		return mirrorStack;
	}
}