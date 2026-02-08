package arcana.items;

import arcana.blocks.be.MagicMirrorBlockEntity;
import arcana.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
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
		NbtCompound nbt = stack.getNbt();
		if(!world.isClient && nbt != null && nbt.getBoolean("bundled")){
			setTag(stack, MathUtil.randomUuid(world.random));
			nbt.remove("bundled");
			stack.setCount(2);
		}
	}
	
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks){
		if(isIn(group)){
			ItemStack stack = getDefaultStack();
			stack.getOrCreateNbt().putBoolean("bundled", true);
			stacks.add(stack);
		}
	}
	
	public static @Nullable UUID getTag(ItemStack mirrorStack){
		NbtCompound nbt = mirrorStack.getNbt();
		return nbt != null && nbt.containsUuid("tag") ? nbt.getUuid("tag") : null;
	}
	
	protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state){
		if(!world.isClient && world.getBlockEntity(pos) instanceof MagicMirrorBlockEntity mm){
			UUID tag = getTag(stack);
			mm.setTag(tag != null ? tag : MathUtil.randomUuid(world.random));
			return true;
		}
		return super.postPlacement(pos, world, player, stack, state);
	}
	
	public static ItemStack setTag(ItemStack mirrorStack, UUID uuid){
		mirrorStack.getOrCreateNbt().putUuid("tag", uuid);
		return mirrorStack;
	}
}