package arcana.items;

import arcana.ArcanaRegistry;
import arcana.components.MagicMirrorQueue;
import arcana.util.MathUtil;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
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

import java.util.Optional;
import java.util.UUID;

public class PersonalMagicMirrorItem extends Item{
	
	public static final UUID ID = UUID.nameUUIDFromBytes(new byte[0]);
	
	public PersonalMagicMirrorItem(Settings settings){
		super(settings);
	}
	
	public Optional<TooltipData> getTooltipData(ItemStack stack){
		return Optional.of(new MagicMirrorTooltipData(MagicMirrorBlockItem.getTag(stack)));
	}
	
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
		NbtCompound nbt = stack.getNbt();
		if(!world.isClient && nbt != null && nbt.getBoolean("bundled")){
			UUID tag = MathUtil.randomUuid(world.random);
			MagicMirrorBlockItem.setTag(stack, tag);
			nbt.remove("bundled");
			if(entity instanceof PlayerEntity pe)
				pe.giveItemStack(MagicMirrorBlockItem.setTag(new ItemStack(ArcanaRegistry.MAGIC_MIRROR), tag));
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
				MagicMirrorQueue.from(player.world).push(targetTag, ID, otherStack);
				ref.set(ItemStack.EMPTY);
				// TODO: SFX
				return true;
			}
		}
		return super.onClicked(stack, otherStack, slot, clickType, player, ref);
	}
}