package arcana;

import arcana.blocks.ArcaneCraftingTableBlock;
import arcana.blocks.CrucibleBlock;
import arcana.items.*;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
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
	
	public static final CapItem IRON_WAND_CAP = new CapItem(new Settings().group(Tab.ARCANA));
	public static final CapItem COPPER_WAND_CAP = new CapItem(new Settings().group(Tab.ARCANA));
	public static final CapItem GOLD_WAND_CAP = new CapItem(new Settings().group(Tab.ARCANA));
	
	public static final CoreItem BONE_WAND_CORE = new CoreItem(new Settings().group(Tab.ARCANA));
	public static final CoreItem BLAZE_WAND_CORE = new CoreItem(new Settings().group(Tab.ARCANA));
	public static final CoreItem ICE_WAND_CORE = new CoreItem(new Settings().group(Tab.ARCANA));
	public static final Core STICK_CORE = new Core.Impl(arcId("stick_wand_core"));
	
	public static final Item WAND = new WandItem(new Settings().group(Tab.ARCANA));
	
	public static final Block ARCANE_CRAFTING_TABLE = new ArcaneCraftingTableBlock(FabricBlockSettings.of(Material.WOOD));
	public static final Block CRUCIBLE = new CrucibleBlock(FabricBlockSettings.of(Material.METAL));
	
	public static final List<Item> ITEMS = new ArrayList<>();
	public static final List<Block> BLOCKS = new ArrayList<>();
	
	public static void setup(){
		register("iron_wand_cap", IRON_WAND_CAP);
		register("copper_wand_cap", COPPER_WAND_CAP);
		register("gold_wand_cap", GOLD_WAND_CAP);
		
		register("bone_wand_core", BONE_WAND_CORE);
		register("blaze_wand_core", BLAZE_WAND_CORE);
		register("ice_wand_core", ICE_WAND_CORE);
		registerCoreOnly(STICK_CORE);
		
		register("wand", WAND);
		
		register("arcane_crafting_table", ARCANE_CRAFTING_TABLE);
		register("crucible", CRUCIBLE);
	}
	
	private static void register(String name, Item item){
		Registry.register(Registry.ITEM, arcId(name), item);
		ITEMS.add(item);
		if(item instanceof Cap c)
			registerCapOnly(c);
		if(item instanceof Core c)
			registerCoreOnly(c);
	}
	
	private static void register(String name, Block block){
		Registry.register(Registry.BLOCK, arcId(name), block);
		BLOCKS.add(block);
		register(name, new BlockItem(block, new Settings().group(Tab.ARCANA)));
	}
	
	private static void registerCapOnly(Cap cap){
		Cap.CAPS.put(cap.id(), cap);
	}
	
	private static void registerCoreOnly(Core core){
		Core.CORES.put(core.id(), core);
	}
}