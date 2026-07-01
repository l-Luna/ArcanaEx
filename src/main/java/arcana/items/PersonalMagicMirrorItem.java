package arcana.items;

import arcana.ArcanaRegistry;
import arcana.api.ContextCraftedItem;
import arcana.api.CustomCreativePresentationItem;
import arcana.cca_components.MagicMirrorQueue;
import arcana.items.components.ArcanaDataComponents;
import arcana.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class PersonalMagicMirrorItem extends Item implements ContextCraftedItem, CustomCreativePresentationItem{
	
	public PersonalMagicMirrorItem(Settings settings){
		super(settings);
	}
	
	public Optional<TooltipData> getTooltipData(ItemStack stack){
		return Optional.of(new MagicMirrorTooltipData(MagicMirrorBlockItem.getTag(stack)));
	}
	
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
		if(!world.isClient){
			if(getId(stack) == null)
				setId(stack, MathUtil.randomUuid(world.random));
			if(stack.getOrDefault(ArcanaDataComponents.MAGIC_MIRROR_BUNDLED_FLAG, false)){
				UUID tag = MathUtil.randomUuid(world.random);
				MagicMirrorBlockItem.setTag(stack, tag);
				stack.set(ArcanaDataComponents.MAGIC_MIRROR_BUNDLED_FLAG, false);
				if(entity instanceof PlayerEntity player)
					player.giveItemStack(MagicMirrorBlockItem.setTag(new ItemStack(ArcanaRegistry.MAGIC_MIRROR), tag));
			}
			
			if(entity instanceof PlayerEntity player){
				ItemStack next = MagicMirrorQueue.from(world).pull(MagicMirrorBlockItem.getTag(stack), getId(stack));
				if(next != null){
					if(!player.giveItemStack(next)){
						ItemEntity ie = player.dropItem(next, false);
						if(ie != null){
							ie.resetPickupDelay();
							ie.setOwner(player.getUuid());
						}
					}
				}
			}
		}
	}
	
	public void addToTab(ItemGroup.Entries entries){
		ItemStack stack = getDefaultStack();
		stack.set(ArcanaDataComponents.MAGIC_MIRROR_BUNDLED_FLAG, true);
		entries.add(stack);
	}
	
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference ref){
		if(player.getWorld().isClient)
			return false; // TODO: creative inventory
		if(clickType == ClickType.RIGHT && !otherStack.isEmpty()){
			UUID targetTag = MagicMirrorBlockItem.getTag(stack);
			if(targetTag != null){
				MagicMirrorQueue.from(player.getWorld()).push(targetTag, getId(stack), otherStack);
				ref.set(ItemStack.EMPTY);
				// TODO: SFX
				return true;
			}
		}
		return super.onClicked(stack, otherStack, slot, clickType, player, ref);
	}
	
	public static @Nullable UUID getId(ItemStack mirrorStack){
		return mirrorStack.getOrDefault(ArcanaDataComponents.MAGIC_MIRROR_ID, null);
	}
	
	public static ItemStack setId(ItemStack mirrorStack, UUID uuid){
		mirrorStack.set(ArcanaDataComponents.MAGIC_MIRROR_ID, uuid);
		return mirrorStack;
	}
	
	public void onCraft(ItemStack stack, Inventory context, World world, PlayerEntity player){
		for(int i = 0; i < context.size(); i++){
			ItemStack there = context.getStack(i);
			if(there.getItem() instanceof MagicMirrorBlockItem){
				MagicMirrorBlockItem.setTag(stack, MagicMirrorBlockItem.getTag(there));
				break;
			}
		}
	}
}