package arcana;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

public class ArcanaRegistry{
	
	public static class Tab{
		// tfw "illegal forward reference"
		public static final ItemGroup ARCANA = FabricItemGroupBuilder.build(
				arcId("group"),
				() -> new ItemStack(IRON_WAND_CAP)
		);
	}
	
	public static final Item IRON_WAND_CAP = new Item(new Settings().group(Tab.ARCANA));
	public static final Item COPPER_WAND_CAP = new Item(new Settings().group(Tab.ARCANA));
	public static final Item GOLD_WAND_CAP = new Item(new Settings().group(Tab.ARCANA));
	
	public static final Item BONE_WAND_CORE = new Item(new Settings().group(Tab.ARCANA));
	public static final Item BLAZE_WAND_CORE = new Item(new Settings().group(Tab.ARCANA));
	public static final Item ICE_WAND_CORE = new Item(new Settings().group(Tab.ARCANA));
	
	public static final List<Item> ITEMS = new ArrayList<>();
	
	public static void setup(){
		register("iron_wand_cap", IRON_WAND_CAP);
		register("copper_wand_cap", COPPER_WAND_CAP);
		register("gold_wand_cap", GOLD_WAND_CAP);
		
		register("bone_wand_core", BONE_WAND_CORE);
		register("blaze_wand_core", BLAZE_WAND_CORE);
		register("ice_wand_core", ICE_WAND_CORE);
	}
	
	private static void register(String name, Item item){
		Registry.register(Registry.ITEM, arcId(name), item);
		ITEMS.add(item);
	}
}