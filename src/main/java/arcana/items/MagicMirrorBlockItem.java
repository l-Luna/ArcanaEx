package arcana.items;

import arcana.api.CustomCreativePresentationItem;
import arcana.items.components.ArcanaDataComponents;
import arcana.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class MagicMirrorBlockItem extends BlockItem implements CustomCreativePresentationItem{
	
	public MagicMirrorBlockItem(Block block, Settings settings){
		super(block, settings);
	}
	
	public Optional<TooltipData> getTooltipData(ItemStack stack){
		return Optional.of(new MagicMirrorTooltipData(MagicMirrorBlockItem.getTag(stack)));
	}
	
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
		if(!world.isClient && stack.getOrDefault(ArcanaDataComponents.MAGIC_MIRROR_BUNDLED_FLAG, false)){
			setTag(stack, MathUtil.randomUuid(world.random));
			stack.set(ArcanaDataComponents.MAGIC_MIRROR_BUNDLED_FLAG, false);
			stack.setCount(2);
		}
	}
	
	public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player){
		setTag(stack, MathUtil.randomUuid(world.random));
	}
	
	public void addToTab(ItemGroup.Entries entries){
		ItemStack stack = getDefaultStack();
		stack.set(ArcanaDataComponents.MAGIC_MIRROR_BUNDLED_FLAG, true);
		entries.add(stack);
	}
	
	public static @Nullable UUID getTag(ItemStack mirrorStack){
		return mirrorStack.getOrDefault(ArcanaDataComponents.MAGIC_MIRROR_TAG, null);
	}
	
	public static ItemStack setTag(ItemStack mirrorStack, UUID uuid){
		mirrorStack.set(ArcanaDataComponents.MAGIC_MIRROR_TAG, uuid);
		return mirrorStack;
	}
}