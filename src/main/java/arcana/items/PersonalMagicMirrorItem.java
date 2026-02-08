package arcana.items;

import arcana.ArcanaRegistry;
import arcana.components.MagicMirrorQueue;
import arcana.util.MathUtil;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class PersonalMagicMirrorItem extends Item{
	
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
			NbtCompound nbt = stack.getOrCreateNbt();
			if(nbt.getBoolean("bundled")){
				UUID tag = MathUtil.randomUuid(world.random);
				MagicMirrorBlockItem.setTag(stack, tag);
				nbt.remove("bundled");
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
	
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks){
		if(isIn(group)){
			ItemStack stack = getDefaultStack();
			stack.getOrCreateNbt().putBoolean("bundled", true);
			stacks.add(stack);
		}
	}
	
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference ref){
		if(player.world.isClient)
			return false; // TODO: creative inventory
		if(clickType == ClickType.RIGHT && !otherStack.isEmpty()){
			UUID targetTag = MagicMirrorBlockItem.getTag(stack);
			if(targetTag != null){
				MagicMirrorQueue.from(player.world).push(targetTag, getId(stack), otherStack);
				ref.set(ItemStack.EMPTY);
				// TODO: SFX
				return true;
			}
		}
		return super.onClicked(stack, otherStack, slot, clickType, player, ref);
	}
	
	public static @Nullable UUID getId(ItemStack mirrorStack){
		NbtCompound nbt = mirrorStack.getNbt();
		return nbt != null && nbt.containsUuid("id") ? nbt.getUuid("id") : null;
	}
	
	public static ItemStack setId(ItemStack mirrorStack, UUID uuid){
		mirrorStack.getOrCreateNbt().putUuid("id", uuid);
		return mirrorStack;
	}
}