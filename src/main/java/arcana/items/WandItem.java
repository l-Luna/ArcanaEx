package arcana.items;

import arcana.ArcanaRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class WandItem extends Item{
	
	public WandItem(Settings settings){
		super(settings);
	}
	
	public static ItemStack withCapAndCore(Cap cap, Core core){
		ItemStack stack = new ItemStack(ArcanaRegistry.WAND);
		NbtCompound tag = stack.getOrCreateNbt();
		tag.putString("cap_id", cap.id().toString());
		tag.putString("core_id", core.id().toString());
		return stack;
	}
	
	public Text getName(ItemStack stack){
		return Text.translatable(
				"item.arcana.wand",
				Text.translatable(capFrom(stack).translationKey()),
				Text.translatable(coreFrom(stack).translationKey()),
				Text.translatable("wand.variant.arcana.wand"));
	}
	
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks){
		if(isIn(group)){
			stacks.add(withCapAndCore(ArcanaRegistry.IRON_WAND_CAP, ArcanaRegistry.STICK_CORE));
			stacks.add(withCapAndCore(ArcanaRegistry.GOLD_WAND_CAP, ArcanaRegistry.BLAZE_WAND_CORE));
		}
	}
	
	private static Cap capFrom(ItemStack stack){
		return capFrom(stack.getOrCreateNbt());
	}
	
	private static Cap capFrom(NbtCompound nbt){
		return Cap.CAPS.get(new Identifier(nbt.getString("cap_id")));
	}
	
	private static Core coreFrom(ItemStack stack){
		return coreFrom(stack.getOrCreateNbt());
	}
	
	private static Core coreFrom(NbtCompound nbt){
		return Core.CORES.get(new Identifier(nbt.getString("core_id")));
	}
}